# mall 项目设计模式重构文档

## 一、概述

对 mall 后端项目进行设计模式重构，提升代码可扩展性和可维护性。主要应用了两种设计模式：

- **工厂模式 + 策略模式**：消除 if-else 分支，实现开闭原则
- **责任链模式**：将长流程拆分为独立的处理器，顺序执行

---

## 二、工厂+策略模式

### 2.1 促销计算重构

**原有问题**：`OmsPromotionServiceImpl.calcCartPromotion()` 中有 5 种促销类型的 if-else 分支，代码大量重复。

#### （1）整体思路

改造之后，不在 Service 中写促销计算逻辑，让 Service 调用工厂，然后通过促销类型参数来获取不同的促销策略。

#### （2）具体实现

**抽象策略类：PromotionStrategy**

```java
public interface PromotionStrategy {
    CartPromotionItem calculatePromotion(PromotionContext context);
}
```

**具体的策略：FlashPromotionStrategy、SingleItemPromotionStrategy、LadderDiscountPromotionStrategy 等**

```java
// 策略：单品促销
@Component
public class SingleItemPromotionStrategy implements PromotionStrategy {
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        CartPromotionItem item = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), item);
        item.setPromotionMessage("单品促销");
        
        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), 
                                                 context.getItem().getProductSkuId());
        if (skuStock == null) return null;
        
        BigDecimal originalPrice = skuStock.getPrice();
        item.setPrice(originalPrice);
        item.setReduceAmount(originalPrice.subtract(skuStock.getPromotionPrice()));
        
        int lockStock = skuStock.getLockStock() == null ? 0 : skuStock.getLockStock();
        item.setRealStock(skuStock.getStock() - lockStock);
        item.setIntegration(context.getPromotionProduct().getGiftPoint());
        item.setGrowth(context.getPromotionProduct().getGiftGrowth());
        
        return item;
    }
}
```

```java
// 策略：打折优惠
@Component
public class LadderDiscountPromotionStrategy implements PromotionStrategy {
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        PmsProductLadder ladder = context.getLadder();
        if (ladder == null) return null;
        
        CartPromotionItem item = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), item);
        item.setPromotionMessage("打折优惠：满" + ladder.getCount() + "件，打" 
                                 + ladder.getDiscount().multiply(new BigDecimal(10)) + "折");
        
        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), 
                                                 context.getItem().getProductSkuId());
        if (skuStock == null) return null;
        
        BigDecimal originalPrice = skuStock.getPrice();
        item.setReduceAmount(originalPrice.subtract(ladder.getDiscount().multiply(originalPrice)));
        // ... 设置库存、积分等
        return item;
    }
}
```

```java
// 策略：满减优惠
@Component
public class FullReductionPromotionStrategy implements PromotionStrategy {
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        PmsProductFullReduction fullReduction = context.getFullReduction();
        if (fullReduction == null) return null;
        
        CartPromotionItem item = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), item);
        item.setPromotionMessage("满减优惠：满" + fullReduction.getFullPrice() + "元，减" 
                                 + fullReduction.getReducePrice() + "元");
        
        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), 
                                                 context.getItem().getProductSkuId());
        if (skuStock == null) return null;
        
        BigDecimal originalPrice = skuStock.getPrice();
        BigDecimal totalAmount = context.getTotalAmount();
        item.setReduceAmount(originalPrice.divide(totalAmount, RoundingMode.HALF_EVEN)
                             .multiply(fullReduction.getReducePrice()));
        // ... 设置库存、积分等
        return item;
    }
}
```

**工厂类：PromotionStrategyFactory**

```java
@Component
public class PromotionStrategyFactory implements ApplicationContextAware {

    private static final Map<String, PromotionStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private PromotionTypeConfig promotionTypeConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        promotionTypeConfig.getTypes().forEach((key, beanName) -> {
            strategyPool.put(key, (PromotionStrategy) applicationContext.getBean(beanName));
        });
    }

    public PromotionStrategy getStrategy(Integer promotionType) {
        String key = String.valueOf(promotionType);
        PromotionStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            return strategyPool.get("0");  // 默认无优惠
        }
        return strategy;
    }
}
```

**在 application.yml 文件中新增自定义配置**

```yaml
# 促销策略映射：promotionType值 -> 策略Bean名称
promotion:
  types:
    "-1": flashPromotionStrategy
    "0": noPromotionStrategy
    "1": singleItemPromotionStrategy
    "3": ladderDiscountPromotionStrategy
    "4": fullReductionPromotionStrategy
```

**新增读取数据配置类**

```java
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "promotion")
public class PromotionTypeConfig {
    private Map<String, String> types;
}
```

**改造 Service 代码**

```java
@Service
@RequiredArgsConstructor
public class OmsPromotionServiceImpl implements OmsPromotionService {
    private final PromotionStrategyFactory promotionStrategyFactory;

    @Override
    public List<CartPromotionItem> calcCartPromotion(List<OmsCartItem> cartItemList) {
        // ... 分组和查询逻辑 ...
        
        PromotionStrategy strategy = promotionStrategyFactory.getStrategy(promotionType);
        for (OmsCartItem item : itemList) {
            PromotionContext context = PromotionContext.builder()
                    .item(item)
                    .promotionProduct(promotionProduct)
                    .ladder(ladder)
                    .fullReduction(fullReduction)
                    .totalAmount(totalAmount)
                    .build();
            CartPromotionItem result = strategy.calculatePromotion(context);
            if (result != null) {
                cartPromotionItemList.add(result);
            }
        }
    }
}
```

---

### 2.2 优惠券适用范围重构

**原有问题**：`useType`（全场/分类/商品）的 if-else 分支在多处重复出现。

#### （1）整体思路

改造之后，优惠券适用范围的判断逻辑通过工厂+策略模式管理，Service 只需调用工厂获取策略。

#### （2）具体实现

**抽象策略类：CouponScopeStrategy**

```java
public interface CouponScopeStrategy {
    BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail detail);
    List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail detail, List<OmsOrderItem> orderItemList);
}
```

**具体的策略：**

```java
// 策略：全场通用
@Component
public class AllProductCouponScopeStrategy implements CouponScopeStrategy {
    @Override
    public BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail detail) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }
    
    @Override
    public List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail detail, List<OmsOrderItem> orderItemList) {
        return orderItemList;  // 全部符合
    }
}
```

```java
// 策略：指定分类
@Component
public class CategoryCouponScopeStrategy implements CouponScopeStrategy {
    @Override
    public BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail detail) {
        List<Long> categoryIds = extractCategoryIds(detail);
        BigDecimal total = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            if (!categoryIds.contains(item.getProductCategoryId())) continue;
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }
    
    @Override
    public List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail detail, List<OmsOrderItem> orderItemList) {
        List<Long> categoryIdList = extractCategoryIds(detail);
        List<OmsOrderItem> result = new ArrayList<>();
        for (OmsOrderItem item : orderItemList) {
            if (categoryIdList.contains(item.getProductCategoryId())) {
                result.add(item);
            } else {
                item.setCouponAmount(new BigDecimal(0));
            }
        }
        return result;
    }
}
```

**工厂类：CouponScopeStrategyFactory**

```java
@Component
public class CouponScopeStrategyFactory implements ApplicationContextAware {

    private static final Map<String, CouponScopeStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private CouponTypeConfig couponTypeConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        couponTypeConfig.getScopeTypes().forEach((key, beanName) -> {
            strategyPool.put(key, (CouponScopeStrategy) applicationContext.getBean(beanName));
        });
    }

    public CouponScopeStrategy getStrategy(Integer useType) {
        String key = String.valueOf(useType);
        CouponScopeStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            return strategyPool.get("0");  // 默认全场通用
        }
        return strategy;
    }
}
```

**在 application.yml 文件中新增自定义配置**

```yaml
# 优惠券适用范围策略映射
coupon:
  scopeTypes:
    "0": allProductCouponScopeStrategy
    "1": categoryCouponScopeStrategy
    "2": productCouponScopeStrategy
```

**改造 Service 代码**

```java
@Service
@RequiredArgsConstructor
public class UmsMemberCouponServiceImpl implements UmsMemberCouponService {
    private final CouponScopeStrategyFactory couponScopeStrategyFactory;

    public List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type) {
        // ...
        for (SmsCouponHistoryDetail couponHistoryDetail : allList) {
            Integer useType = couponHistoryDetail.getCoupon().getUseType();
            
            CouponScopeStrategy strategy = couponScopeStrategyFactory.getStrategy(useType);
            BigDecimal totalAmount = strategy.calcEligibleAmount(cartItemList, couponHistoryDetail);
            
            if (!isExpired && totalAmount.compareTo(minPoint) >= 0) {
                enableList.add(couponHistoryDetail);
            } else {
                disableList.add(couponHistoryDetail);
            }
        }
    }
}
```

---

### 2.3 支付网关重构

**原有问题**：`AlipayServiceImpl` 硬编码支付宝逻辑，扩展微信支付需大量修改。

#### （1）整体思路

支付策略通过配置文件管理，新增支付方式只需添加策略类 + 配置映射。

#### （2）具体实现

**抽象策略类：PaymentStrategy**

```java
public interface PaymentStrategy {
    String pay(PaymentParam param);
    String webPay(PaymentParam param);
    String notify(Map<String, String> params);
    String query(String outTradeNo, String tradeNo);
}
```

**具体的策略：AlipayPaymentStrategy**

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentStrategy implements PaymentStrategy {

    private final AlipayConfig alipayConfig;
    private final AlipayClient alipayClient;
    private final OmsPortalOrderService portalOrderService;

    @Override
    public String pay(PaymentParam param) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if (StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())) {
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if (StrUtil.isNotEmpty(alipayConfig.getReturnUrl())) {
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", param.getOutTradeNo());
        bizContent.put("total_amount", param.getTotalAmount().toPlainString());
        bizContent.put("subject", param.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝电脑支付异常", e);
            return null;
        }
    }
    
    // ... 其他方法实现
}
```

**工厂类：PaymentStrategyFactory**

```java
@Component
public class PaymentStrategyFactory implements ApplicationContextAware {

    private static final Map<String, PaymentStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private PaymentTypeConfig paymentTypeConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        paymentTypeConfig.getTypes().forEach((key, beanName) -> {
            strategyPool.put(key, (PaymentStrategy) applicationContext.getBean(beanName));
        });
    }

    public PaymentStrategy getStrategy(Integer payType) {
        String key = String.valueOf(payType);
        PaymentStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            throw new UnsupportedOperationException("不支持的支付方式: " + payType);
        }
        return strategy;
    }
}
```

**在 application.yml 文件中新增自定义配置**

```yaml
# 支付方式策略映射：payType值 -> 策略Bean名称
payment:
  types:
    "1": alipayPaymentStrategy
    # "2": wechatPaymentStrategy  # 未来新增微信支付
```

**新增读取数据配置类**

```java
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentTypeConfig {
    private Map<String, String> types;
}
```

**改造 Controller 代码**

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/pay")
public class AlipayController {

    private final PaymentStrategyFactory paymentStrategyFactory;

    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public void pay(AliPayParam aliPayParam,
                    @RequestParam(defaultValue = "1") Integer payType,
                    HttpServletResponse response) throws IOException {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(payType);
        PaymentParam param = buildParam(aliPayParam, payType);
        response.getWriter().write(strategy.pay(param));
    }
}
```

---

## 三、责任链模式

### 3.1 订单生成流程

**原有问题**：`generateOrder()` 方法长达 186 行，包含 13 个顺序步骤。

#### （1）整体思路

将订单生成的每个步骤封装成独立的 Handler，通过责任链串联执行。

#### （2）具体实现

**抽象处理者：OrderHandler**

```java
public abstract class OrderHandler {
    protected OrderHandler next;

    public void setNext(OrderHandler next) {
        this.next = next;
    }

    public abstract void handle(OrderHandlerContext context);

    protected void handleNext(OrderHandlerContext context) {
        if (next != null) {
            next.handle(context);
        }
    }
}
```

**具体处理者示例：**

```java
// 校验库存
public class CheckStockHandler extends OrderHandler {
    @Override
    public void handle(OrderHandlerContext context) {
        List<CartPromotionItem> cartItems = context.getCartPromotionItemList();
        for (CartPromotionItem item : cartItems) {
            if (item.getRealStock() == null || item.getRealStock() < item.getQuantity()) {
                Asserts.fail("库存不足，无法下单");
            }
        }
        handleNext(context);
    }
}
```

```java
// 处理优惠券
@Component
@RequiredArgsConstructor
public class HandleCouponHandler extends OrderHandler {
    private final UmsMemberCouponService memberCouponService;
    private final CouponScopeStrategyFactory couponScopeStrategyFactory;

    @Override
    public void handle(OrderHandlerContext context) {
        Long couponId = context.getAttribute("couponId");
        if (couponId != null) {
            // 处理优惠券逻辑...
        }
        handleNext(context);
    }
}
```

```java
// 计算实付金额
public class HandleRealAmountHandler extends OrderHandler {
    @Override
    public void handle(OrderHandlerContext context) {
        for (OmsOrderItem item : context.getOrderItemList()) {
            BigDecimal realAmount = item.getProductPrice()
                    .subtract(item.getPromotionAmount())
                    .subtract(item.getCouponAmount())
                    .subtract(item.getIntegrationAmount());
            item.setRealAmount(realAmount);
        }
        handleNext(context);
    }
}
```

**链执行器：OrderCreationChain**

```java
public class OrderCreationChain {
    private final List<OrderHandler> handlers = new ArrayList<>();

    public OrderCreationChain addHandler(OrderHandler handler) {
        handlers.add(handler);
        return this;
    }

    public void execute(OrderHandlerContext context) {
        if (handlers.isEmpty()) return;
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
        handlers.get(0).handle(context);
    }
}
```

**改造 Service 代码**

```java
@Service
@RequiredArgsConstructor
public class OmsPortalOrderServiceImpl implements OmsPortalOrderService {
    private final CouponScopeStrategyFactory couponScopeStrategyFactory;

    public Map<String, Object> generateOrder(OrderParam orderParam) {
        OrderHandlerContext context = new OrderHandlerContext();
        context.putAttribute("cartIds", orderParam.getCartIds());
        context.putAttribute("couponId", orderParam.getCouponId());
        // ... 设置其他参数

        OrderCreationChain chain = new OrderCreationChain();
        chain.addHandler(new ValidateAddressHandler(memberReceiveAddressService));
        chain.addHandler(new LoadCartHandler(memberService, cartItemService));
        chain.addHandler(new FlashValidationHandler(...));
        chain.addHandler(new BuildOrderItemsHandler());
        chain.addHandler(new CheckStockHandler());
        chain.addHandler(new HandleCouponHandler(memberCouponService, couponScopeStrategyFactory));
        chain.addHandler(new HandleIntegrationHandler(integrationConsumeSettingMapper));
        chain.addHandler(new HandleRealAmountHandler());
        chain.addHandler(new LockStockHandler(skuStockMapper));
        chain.addHandler(new BuildOrderHandler(orderSettingMapper));
        chain.addHandler(new PersistOrderHandler(orderMapper, orderItemDao));
        chain.addHandler(new PostOrderHandler(...));
        chain.execute(context);

        Map<String, Object> result = new HashMap<>();
        result.put("order", context.getOrder());
        result.put("orderItemList", context.getOrderItemList());
        return result;
    }
}
```

---

### 3.2 支付成功处理

#### （1）整体思路

支付成功的处理流程拆分为独立的 Handler：加载订单 → 更新状态 → 扣减库存 → 更新销量 → 更新优惠券。

#### （2）具体实现

```java
public Integer paySuccess(Long orderId, Integer payType) {
    OrderHandlerContext context = new OrderHandlerContext();
    context.putAttribute("orderId", orderId);
    context.putAttribute("payType", payType);

    OrderCreationChain chain = new OrderCreationChain();
    chain.addHandler(new LoadOrderHandler(orderMapper));
    chain.addHandler(new UpdatePaymentStatusHandler(orderMapper));
    chain.addHandler(new LoadOrderDetailHandler(portalOrderDao));
    chain.addHandler(new DeductStockHandler(portalOrderDao, dailyStockMapper, flashPromotionProductRelationMapper, redisService));
    chain.addHandler(new UpdateSalesHandler(portalOrderDao, redisService));
    chain.addHandler(new UpdateCouponStatusForPayHandler(couponHistoryMapper, couponMapper));
    chain.execute(context);

    List<OmsOrderItem> orderItemList = context.getAttribute("orderItemList");
    return orderItemList != null ? orderItemList.size() : 0;
}
```

---

### 3.3 订单取消流程

#### （1）整体思路

订单取消流程拆分为：加载校验 → 更新状态 → 恢复库存 → 恢复优惠券 → 返还积分。

#### （2）具体实现

```java
public void cancelOrder(Long orderId) {
    OrderHandlerContext context = new OrderHandlerContext();
    context.putAttribute("cancelOrderId", orderId);

    OrderCreationChain chain = new OrderCreationChain();
    chain.addHandler(new LoadAndValidateCancelOrderHandler(orderMapper));
    chain.addHandler(new UpdateCancelStatusHandler(orderMapper, orderItemMapper));
    chain.addHandler(new RestoreStockForCancelHandler(portalOrderDao, dailyStockMapper));
    chain.addHandler(new RestoreCouponForCancelHandler(couponHistoryMapper, couponMapper));
    chain.addHandler(new ReturnIntegrationHandler(memberService));
    chain.execute(context);
}
```

---

## 四、文件变更汇总

### 新增文件

| 分类 | 文件 |
|---|---|
| 配置类 | `PromotionTypeConfig.java`, `CouponTypeConfig.java`, `PaymentTypeConfig.java` |
| 促销策略 | `PromotionStrategy.java`, `PromotionContext.java`, `PromotionStrategyFactory.java`, 5个策略实现类 |
| 优惠券策略 | `CouponScopeStrategy.java`, `CouponScopeStrategyFactory.java`, 3个策略实现类 |
| 支付策略 | `PaymentStrategy.java`, `PaymentParam.java`, `PaymentStrategyFactory.java`, `AlipayPaymentStrategy.java` |
| 责任链 | `OrderHandler.java`, `OrderHandlerContext.java`, `OrderCreationChain.java`, 17个Handler实现类 |

### 修改文件

| 文件 | 变更内容 |
|---|---|
| `application-dev.yml` | 新增 promotion、coupon、payment 策略映射配置 |
| `OmsPromotionServiceImpl.java` | 注入工厂，改为策略调用 |
| `UmsMemberCouponServiceImpl.java` | 注入工厂，改为策略调用 |
| `OmsPortalOrderServiceImpl.java` | 注入工厂，改为责任链+策略调用 |
| `AlipayController.java` | 注入工厂，改为通过工厂路由支付方式 |

---

## 五、扩展指南

### 新增促销类型
1. 实现 `PromotionStrategy` 接口并加 `@Component`
2. 在 `application.yml` 的 `promotion.types` 中加一行配置
3. 无需修改 Java 代码

### 新增优惠券适用范围
1. 实现 `CouponScopeStrategy` 接口并加 `@Component`
2. 在 `application.yml` 的 `coupon.scopeTypes` 中加一行配置

### 新增支付方式
1. 实现 `PaymentStrategy` 接口并加 `@Component`
2. 在 `application.yml` 的 `payment.types` 中加一行配置

### 新增订单处理步骤
1. 继承 `OrderHandler` 实现 `handle()` 方法
2. 在构建链时通过 `addHandler()` 添加
