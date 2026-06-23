package com.su.mall.portal.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.exception.Asserts;
import com.su.mall.common.service.RedisService;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.portal.component.CancelOrderSender;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.portal.dao.PortalOrderItemDao;

import com.su.mall.portal.domain.*;
import com.su.mall.portal.service.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 前台订单管理Service
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class OmsPortalOrderServiceImpl implements OmsPortalOrderService {
    private final UmsMemberService memberService;
    private final OmsCartItemService cartItemService;
    private final UmsMemberReceiveAddressService memberReceiveAddressService;
    private final UmsMemberCouponService memberCouponService;
    private final UmsIntegrationConsumeSettingMapper integrationConsumeSettingMapper;
    private final PmsSkuStockMapper skuStockMapper;

    private final OmsOrderMapper orderMapper;
    private final PortalOrderItemDao orderItemDao;
    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponMapper couponMapper;
    private final RedisService redisService;
    private final SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    @Value("${redis.key.orderId}")
    private String REDIS_KEY_ORDER_ID;
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    private final PortalOrderDao portalOrderDao;
    private final OmsOrderSettingMapper orderSettingMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final CancelOrderSender cancelOrderSender;

    @Override
    public ConfirmOrderResult generateConfirmOrder(List<Long> cartIds) {
        ConfirmOrderResult result = new ConfirmOrderResult();
        //获取购物车信息
        UmsMember currentMember = memberService.getCurrentMember();
        List<CartPromotionItem> cartPromotionItemList = cartItemService.listPromotion(currentMember.getId(),cartIds);
        result.setCartPromotionItemList(cartPromotionItemList);
        //获取用户收货地址列表
        List<UmsMemberReceiveAddress> memberReceiveAddressList = memberReceiveAddressService.list();
        result.setMemberReceiveAddressList(memberReceiveAddressList);
        //获取用户可用优惠券列表
        List<SmsCouponHistoryDetail> couponHistoryDetailList = memberCouponService.listCart(cartPromotionItemList, 1);
        result.setCouponHistoryDetailList(couponHistoryDetailList);
        //获取用户积分
        result.setMemberIntegration(currentMember.getIntegration());
        //获取积分使用规则
        UmsIntegrationConsumeSetting integrationConsumeSetting = integrationConsumeSettingMapper.selectById(1L);
        result.setIntegrationConsumeSetting(integrationConsumeSetting);
        //计算总金额、活动优惠、应付金额
        ConfirmOrderResult.CalcAmount calcAmount = calcCartAmount(cartPromotionItemList);
        result.setCalcAmount(calcAmount);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> generateOrder(OrderParam orderParam) {
        List<OmsOrderItem> orderItemList = new ArrayList<>();
        //校验收货地址
        if(orderParam.getMemberReceiveAddressId()==null){
            Asserts.fail("请选择收货地址！");
        }
        //获取购物车及优惠信息
        UmsMember currentMember = memberService.getCurrentMember();
        List<CartPromotionItem> cartPromotionItemList = cartItemService.listPromotion(currentMember.getId(), orderParam.getCartIds());

        //判断是否有秒杀商品
        boolean hasFlashPromotion = false;

        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            if (cartPromotionItem.getFlashPromotion() != null && cartPromotionItem.getFlashPromotion()) {
                hasFlashPromotion = true;
                break;
            }
        }

        //秒杀商品库存和限购校验
        if (hasFlashPromotion) {
            for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
                if (cartPromotionItem.getFlashPromotion() != null && cartPromotionItem.getFlashPromotion()) {
                    if (cartPromotionItem.getFlashPromotionRelationId() == null) {
                        Asserts.fail("秒杀商品信息异常");
                    }
                    SmsFlashPromotionProductRelation relation = flashPromotionProductRelationMapper.selectById(
                            cartPromotionItem.getFlashPromotionRelationId());
                    if (relation == null) {
                        Asserts.fail("秒杀活动不存在");
                    }
                    //秒杀库存校验（从每日快照表读取）
                    Integer dailyStock = dailyStockMapper.getCurrentStock(cartPromotionItem.getFlashPromotionRelationId());
                    if (dailyStock == null || dailyStock < cartPromotionItem.getQuantity()) {
                        Asserts.fail("秒杀库存不足");
                    }
                    //限购数量校验
                    if (relation.getFlashPromotionLimit() != null && cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                        Asserts.fail("超出限购数量，每人限购" + relation.getFlashPromotionLimit() + "件");
                    }
                    //当前账号已购数量校验
                    int buyCount = getMemberFlashBuyCount(currentMember.getId(), cartPromotionItem.getFlashPromotionRelationId());
                    if (relation.getFlashPromotionLimit() != null && buyCount + cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                        Asserts.fail("超出限购数量，您已购买" + buyCount + "件，每人限购" + relation.getFlashPromotionLimit() + "件");
                    }
                }
            }
        }

        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            //生成下单商品信息
            OmsOrderItem orderItem = getOmsOrderItem(cartPromotionItem);
            orderItemList.add(orderItem);
        }
        //判断购物车中商品是否都有库存
        if (!hasStock(cartPromotionItemList)) {
            Asserts.fail("库存不足，无法下单");
        }
        //判断使用使用了优惠券
        if (orderParam.getCouponId() == null) {
            //不用优惠券
            for (OmsOrderItem orderItem : orderItemList) {
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        } else {
            //使用优惠券
            SmsCouponHistoryDetail couponHistoryDetail = getUseCoupon(cartPromotionItemList, orderParam.getCouponId());
            if (couponHistoryDetail == null) {
                Asserts.fail("该优惠券不可用");
            }
            //对下单商品的优惠券进行处理
            handleCouponAmount(orderItemList, couponHistoryDetail);
        }
        //判断是否使用积分
        if (orderParam.getUseIntegration() == null||orderParam.getUseIntegration().equals(0)) {
            //不使用积分
            for (OmsOrderItem orderItem : orderItemList) {
                orderItem.setIntegrationAmount(new BigDecimal(0));
            }
        } else {
            //使用积分
            BigDecimal totalAmount = calcTotalAmount(orderItemList);
            BigDecimal integrationAmount = getUseIntegrationAmount(orderParam.getUseIntegration(), totalAmount, currentMember, orderParam.getCouponId() != null);
            if (integrationAmount.compareTo(new BigDecimal(0)) == 0) {
                Asserts.fail("积分不可用");
            } else {
                //可用情况下分摊到可用商品中
                for (OmsOrderItem orderItem : orderItemList) {
                    BigDecimal perAmount = orderItem.getProductPrice().divide(totalAmount, 3, RoundingMode.HALF_EVEN).multiply(integrationAmount);
                    orderItem.setIntegrationAmount(perAmount);
                }
            }
        }
        //计算order_item的实付金额
        handleRealAmount(orderItemList);
        //进行库存锁定
        lockStock(cartPromotionItemList);
        //根据商品合计、运费、活动优惠、优惠券、积分计算应付金额
        OmsOrder order = new OmsOrder();
        order.setDiscountAmount(new BigDecimal(0));
        order.setTotalAmount(calcTotalAmount(orderItemList));
        order.setFreightAmount(new BigDecimal(0));
        order.setPromotionAmount(calcPromotionAmount(orderItemList));
        order.setPromotionInfo(getOrderPromotionInfo(orderItemList));
        if (orderParam.getCouponId() == null) {
            order.setCouponAmount(new BigDecimal(0));
        } else {
            order.setCouponId(orderParam.getCouponId());
            order.setCouponAmount(calcCouponAmount(orderItemList));
        }
        if (orderParam.getUseIntegration() == null) {
            order.setIntegration(0);
            order.setIntegrationAmount(new BigDecimal(0));
        } else {
            order.setIntegration(orderParam.getUseIntegration());
            order.setIntegrationAmount(calcIntegrationAmount(orderItemList));
        }
        order.setPayAmount(calcPayAmount(order));
        //转化为订单信息并插入数据库
        order.setMemberId(currentMember.getId());
        order.setCreateTime(new Date());
        order.setMemberUsername(currentMember.getUsername());
        //支付方式：0->未支付；1->支付宝；2->微信
        order.setPayType(orderParam.getPayType());
        //订单来源：0->PC订单；1->app订单
        order.setSourceType(1);
        //订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
        order.setStatus(0);
        //订单类型：0->正常订单；1->秒杀订单
        order.setOrderType(hasFlashPromotion ? 1 : 0);
        //收货人信息：姓名、电话、邮编、地址
        UmsMemberReceiveAddress address = memberReceiveAddressService.getItem(orderParam.getMemberReceiveAddressId());
        if (address == null) {
            Asserts.fail("收货地址不存在");
        }
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        //0->未确认；1->已确认
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        //计算赠送积分
        order.setIntegration(calcGifIntegration(orderItemList));
        //计算赠送成长值
        order.setGrowth(calcGiftGrowth(orderItemList));
        //生成订单号
        order.setOrderSn(generateOrderSn(order));
        //设置自动收货天数
        List<OmsOrderSetting> orderSettings = orderSettingMapper.selectList(new LambdaQueryWrapper<>());
        if(CollUtil.isNotEmpty(orderSettings)){
            order.setAutoConfirmDay(orderSettings.get(0).getConfirmOvertime());
        }
        // TODO: 2018/9/3 bill_*,delivery_*
        //插入order表和order_item表
        orderMapper.insert(order);
        for (OmsOrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
        }
        orderItemDao.insertList(orderItemList);
        //如使用优惠券更新优惠券使用状态
        if (orderParam.getCouponId() != null) {
            updateCouponStatus(orderParam.getCouponId(), currentMember.getId(), 1, order.getId(), order.getOrderSn());
        }
        //如使用积分需要扣除积分
        if (orderParam.getUseIntegration() != null) {
            order.setUseIntegration(orderParam.getUseIntegration());
            if(currentMember.getIntegration()==null){
                currentMember.setIntegration(0);
            }
            memberService.updateIntegration(currentMember.getId(), currentMember.getIntegration() - orderParam.getUseIntegration());
        }
        //删除购物车中的下单商品
        deleteCartItemList(cartPromotionItemList, currentMember);
        //发送延迟消息取消订单
        sendDelayMessageCancelOrder(order.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("orderItemList", orderItemList);
        return result;
    }

    // 转换购物车商品为订单商品（修复：积分成长值默认0）
    private static @NonNull OmsOrderItem getOmsOrderItem(CartPromotionItem cartPromotionItem) {
        OmsOrderItem orderItem = new OmsOrderItem();
        orderItem.setProductId(cartPromotionItem.getProductId());
        orderItem.setProductName(cartPromotionItem.getProductName());
        orderItem.setProductPic(cartPromotionItem.getProductPic());
        orderItem.setProductAttr(cartPromotionItem.getProductAttr());
        orderItem.setProductBrand(cartPromotionItem.getProductBrand());
        orderItem.setProductSn(cartPromotionItem.getProductSn());
        orderItem.setProductPrice(cartPromotionItem.getPrice());
        orderItem.setProductQuantity(cartPromotionItem.getQuantity());
        orderItem.setProductSkuId(cartPromotionItem.getProductSkuId());
        orderItem.setProductSkuCode(cartPromotionItem.getProductSkuCode());
        orderItem.setProductCategoryId(cartPromotionItem.getProductCategoryId());
        orderItem.setPromotionAmount(cartPromotionItem.getReduceAmount());
        orderItem.setPromotionName(cartPromotionItem.getPromotionMessage());

        // 空值安全赋值
        orderItem.setGiftIntegration(Objects.requireNonNullElse(cartPromotionItem.getIntegration(), 0));
        orderItem.setGiftGrowth(Objects.requireNonNullElse(cartPromotionItem.getGrowth(), 0));

        orderItem.setFlashPromotionRelationId(cartPromotionItem.getFlashPromotionRelationId());
        return orderItem;
    }

    @Override
    @Transactional
    public Integer paySuccess(Long orderId, Integer payType) {
        // 获取完整订单（只查一次）
        OmsOrder fullOrder = orderMapper.selectById(orderId);
        if (fullOrder == null) {
            Asserts.fail("订单不存在");
        }
        // 校验订单状态，只有待付款订单才能标记为已支付
        if(fullOrder.getStatus() != 0){
            Asserts.fail("订单状态异常，无法支付");
        }
        
        // 更新订单支付状态（复用查询结果）
        fullOrder.setStatus(1);
        fullOrder.setPaymentTime(new Date());
        fullOrder.setPayType(payType);
        orderMapper.updateById(fullOrder);

        // 获取订单详情
        OmsOrderDetail orderDetail = portalOrderDao.getDetail(orderId);
        if (orderDetail == null || CollectionUtils.isEmpty(orderDetail.getOrderItemList())) {
            Asserts.fail("订单详情为空");
        }

        List<OmsOrderItem> orderItemList = orderDetail.getOrderItemList();
        int resultCount = 0;

        if (fullOrder.getOrderType() != null && fullOrder.getOrderType() == 1) {
            // 秒杀订单：按商品维度加锁，防止同一商品并发超卖
            List<String> acquiredLocks = new ArrayList<>();
            String lockValue = UUID.randomUUID().toString();
            try {
                // 按每个秒杀商品分别加锁
                for (OmsOrderItem orderItem : orderItemList) {
                    if (orderItem.getFlashPromotionRelationId() != null) {
                        String lockKey = "flash:lock:relation:" + orderItem.getFlashPromotionRelationId();
                        Boolean locked = redisService.tryLock(lockKey, lockValue, 30);
                        if (!locked) {
                            Asserts.fail("当前抢购人数过多，请稍后重试");
                        }
                        acquiredLocks.add(lockKey);
                    }
                }
                // 扣减秒杀库存（从每日快照表扣减）
                for (OmsOrderItem orderItem : orderItemList) {
                    if (orderItem.getFlashPromotionRelationId() != null) {
                        int result = dailyStockMapper.decreaseStock(
                                orderItem.getFlashPromotionRelationId(),
                                orderItem.getProductQuantity()
                        );
                        if (result == 0) {
                            Asserts.fail("秒杀库存不足");
                        }
                        resultCount++;
                    }
                }
                // 秒杀订单同时扣减商品 SKU 真实库存
                portalOrderDao.updateSkuStock(orderItemList);
                // 扣减商品表库存
                portalOrderDao.updateProductStock(orderItemList);
                // 清除秒杀列表缓存，确保前端获取最新库存
                clearFlashPromotionCache(orderItemList);
            } finally {
                // 释放所有已获取的锁
                for (String lockKey : acquiredLocks) {
                    redisService.releaseLock(lockKey, lockValue);
                }
            }
        } else {
            // 普通订单：扣减真实库存，释放锁定库存
            resultCount = portalOrderDao.updateSkuStock(orderItemList);
            // 扣减商品表库存
            portalOrderDao.updateProductStock(orderItemList);
        }

        // 统一更新销量和清除缓存（消除重复代码）
        updateSalesAndClearCache(orderItemList);

        // 如使用优惠券更新优惠券使用状态为已使用
        if (fullOrder.getCouponId() != null) {
            updateCouponStatus(fullOrder.getCouponId(), fullOrder.getMemberId(), 1, fullOrder.getId(), fullOrder.getOrderSn());
        }

        return resultCount;
    }

    /**
     * 更新销量并清除缓存（复用逻辑）
     */
    private void updateSalesAndClearCache(List<OmsOrderItem> orderItemList) {
        portalOrderDao.updateProductSale(orderItemList);
        portalOrderDao.updateSkuSale(orderItemList);
        clearProductCache(orderItemList);
    }

    /**
     * 清除订单商品的详情缓存
     */
    private void clearProductCache(List<OmsOrderItem> orderItemList) {
        for (OmsOrderItem item : orderItemList) {
            String cacheKey = "product:detail:" + item.getProductId();
            redisService.del(cacheKey);
        }
    }

    /**
     * 清除秒杀列表缓存（支付成功后调用，确保库存实时更新）
     */
    private void clearFlashPromotionCache(List<OmsOrderItem> orderItemList) {
        // 精确删除该订单关联的秒杀活动缓存，避免使用 keys() 通配符
        for (OmsOrderItem item : orderItemList) {
            if (item.getFlashPromotionRelationId() != null) {
                // 通过 relationId 查询关联的活动和场次ID，精确删除缓存
                SmsFlashPromotionProductRelation relation = flashPromotionProductRelationMapper.selectById(item.getFlashPromotionRelationId());
                if (relation != null) {
                    String cacheKey = "home:flashPromotion:" + relation.getFlashPromotionId() + ":" + relation.getFlashPromotionSessionId();
                    redisService.del(cacheKey);
                }
            }
        }
    }

    /**
     * 查询会员在某秒杀场次中已购买的数量（已支付状态）
     */
    private int getMemberFlashBuyCount(Long memberId, Long relationId) {
        return portalOrderDao.getMemberFlashBuyCount(memberId, relationId);
    }

    /**
     * 恢复秒杀商品的库存和已售数量（取消订单时调用）
     */
    private void restoreFlashStock(List<OmsOrderItem> orderItemList) {
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getFlashPromotionRelationId() != null) {
                dailyStockMapper.restoreStock(
                        orderItem.getFlashPromotionRelationId(),
                        orderItem.getProductQuantity()
                );
            }
        }
    }

    /**
     * 恢复SKU库存（已支付订单取消时）
     */
    private void restoreSkuStock(List<OmsOrderItem> orderItemList) {
        portalOrderDao.restoreSkuStock(orderItemList);
    }

    /**
     * 恢复商品表库存（已支付订单取消时）
     */
    private void restoreProductStock(List<OmsOrderItem> orderItemList) {
        portalOrderDao.restoreProductStock(orderItemList);
    }

    @Override
    @Transactional
    public Integer cancelTimeOutOrder() {
        Integer count=0;
        OmsOrderSetting orderSetting = orderSettingMapper.selectById(1L);
        if(orderSetting == null){
            return count;
        }
        //查询超时、未支付的订单及订单详情
        List<OmsOrderDetail> timeOutOrders = portalOrderDao.getTimeOutOrders(orderSetting.getNormalOrderOvertime());
        if (CollectionUtils.isEmpty(timeOutOrders)) {
            return count;
        }
        //修改订单状态为交易取消
        List<Long> ids = new ArrayList<>();
        for (OmsOrderDetail timeOutOrder : timeOutOrders) {
            ids.add(timeOutOrder.getId());
        }
        portalOrderDao.updateOrderStatus(ids, 4);
        for (OmsOrderDetail timeOutOrder : timeOutOrders) {
            //解除订单商品库存锁定
            portalOrderDao.releaseSkuStockLock(timeOutOrder.getOrderItemList());
            //注意：未支付订单的秒杀库存无需恢复（库存仅在支付成功时才扣减）
            //修改优惠券使用状态（恢复为未使用）
            updateCouponStatus(timeOutOrder.getCouponId(), timeOutOrder.getMemberId(), 0, null, null);
            //返还使用积分
            if (timeOutOrder.getUseIntegration() != null) {
                UmsMember member = memberService.getById(timeOutOrder.getMemberId());
                if(member != null){
                    memberService.updateIntegration(timeOutOrder.getMemberId(), member.getIntegration() + timeOutOrder.getUseIntegration());
                }
            }
        }
        return timeOutOrders.size();
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            Asserts.fail("订单不存在！");
        }
        Long memberId = order.getMemberId();
        List<OmsOrder> cancelOrderList = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getId, orderId)
                        .eq(OmsOrder::getMemberId, memberId)
                        .in(OmsOrder::getStatus, 0, 1)
                        .eq(OmsOrder::getDeleteStatus, 0));
        if (CollectionUtils.isEmpty(cancelOrderList)) {
            Asserts.fail("该订单状态不支持取消！");
        }
        OmsOrder cancelOrder = cancelOrderList.get(0);
        if (cancelOrder != null) {
            // 保存原始状态
            Integer originalStatus = cancelOrder.getStatus();
            //修改订单状态为取消
            cancelOrder.setStatus(4);
            orderMapper.updateById(cancelOrder);
            List<OmsOrderItem> orderItemList = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
            if (!CollectionUtils.isEmpty(orderItemList)) {
                if (originalStatus != null && originalStatus == 1) {
                    // 已支付的订单：恢复真实库存 + 商品表库存
                    restoreSkuStock(orderItemList);
                    restoreProductStock(orderItemList);
                } else {
                    // 未支付的订单：只释放锁定库存
                    portalOrderDao.releaseSkuStockLock(orderItemList);
                }
                //恢复秒杀商品库存和已售数量
                restoreFlashStock(orderItemList);
            }
            //修改优惠券使用状态（恢复为未使用）
            updateCouponStatus(cancelOrder.getCouponId(), cancelOrder.getMemberId(), 0, null, null);
            //返还使用积分
            if (cancelOrder.getUseIntegration() != null) {
                UmsMember returnMember = memberService.getById(cancelOrder.getMemberId());
                if(returnMember != null){
                    memberService.updateIntegration(cancelOrder.getMemberId(), returnMember.getIntegration() + cancelOrder.getUseIntegration());
                }
            }
        }
    }

    @Override
    public void sendDelayMessageCancelOrder(Long orderId) {
        //获取订单超时时间
        OmsOrderSetting orderSetting = orderSettingMapper.selectById(1L);
        if(orderSetting == null){
            return;
        }
        long delayTimes = orderSetting.getNormalOrderOvertime() * 60 * 1000;
        //发送延迟消息
        cancelOrderSender.sendMessage(orderId, delayTimes);
    }

    @Override
    @Transactional
    public void confirmReceiveOrder(Long orderId) {
        UmsMember member = memberService.getCurrentMember();
        OmsOrder order = orderMapper.selectById(orderId);
        if(order == null){
            Asserts.fail("订单不存在！");
        }
        if(!member.getId().equals(order.getMemberId())){
            Asserts.fail("不能确认他人订单！");
        }
        if(order.getStatus()!=2){
            Asserts.fail("该订单还未发货！");
        }
        order.setStatus(3);
        order.setConfirmStatus(1);
        order.setReceiveTime(new Date());
        orderMapper.updateById(order);
    }

    @Override
    public CommonPage<OmsOrderDetail> list(Integer status, Integer pageNum, Integer pageSize) {
        if(status==-1){
            status = null;
        }
        UmsMember member = memberService.getCurrentMember();
        Page<OmsOrder> page = new Page<>(pageNum, pageSize);
        Page<OmsOrder> orderPage = orderMapper.selectPage(page,
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getDeleteStatus, 0)
                        .eq(OmsOrder::getMemberId, member.getId())
                        .eq(status != null, OmsOrder::getStatus, status)
                        .orderByDesc(OmsOrder::getCreateTime));

        //设置分页信息
        CommonPage<OmsOrderDetail> resultPage = new CommonPage<>();
        resultPage.setPageNum((int) orderPage.getCurrent());
        resultPage.setPageSize((int) orderPage.getSize());
        resultPage.setTotal(orderPage.getTotal());
        resultPage.setTotalPage((int) orderPage.getPages());
        if(CollUtil.isEmpty(orderPage.getRecords())){
            return resultPage;
        }
        //设置数据信息
        List<Long> orderIds = orderPage.getRecords().stream().map(OmsOrder::getId).collect(Collectors.toList());
        List<OmsOrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().in(OmsOrderItem::getOrderId, orderIds));
        List<OmsOrderDetail> orderDetailList = new ArrayList<>();
        for (OmsOrder omsOrder : orderPage.getRecords()) {
            OmsOrderDetail orderDetail = new OmsOrderDetail();
            BeanUtil.copyProperties(omsOrder,orderDetail);
            List<OmsOrderItem> relatedItemList = orderItemList.stream().filter(item -> item.getOrderId().equals(orderDetail.getId())).collect(Collectors.toList());
            orderDetail.setOrderItemList(relatedItemList);
            orderDetailList.add(orderDetail);
        }
        resultPage.setList(orderDetailList);
        return resultPage;
    }

    @Override
    public OmsOrderDetail detail(Long orderId) {
        UmsMember member = memberService.getCurrentMember();
        OmsOrder omsOrder = orderMapper.selectOne(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getId, orderId)
                        .eq(OmsOrder::getMemberId, member.getId()));
        if(omsOrder == null){
            return null;
        }
        List<OmsOrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        OmsOrderDetail orderDetail = new OmsOrderDetail();
        BeanUtil.copyProperties(omsOrder,orderDetail);
        orderDetail.setOrderItemList(orderItemList);
        return orderDetail;
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        UmsMember member = memberService.getCurrentMember();
        OmsOrder order = orderMapper.selectById(orderId);
        if(order == null){
            Asserts.fail("订单不存在！");
        }
        if(!member.getId().equals(order.getMemberId())){
            Asserts.fail("不能删除他人订单！");
        }
        if(order.getStatus()==3||order.getStatus()==4){
            order.setDeleteStatus(1);
            orderMapper.updateById(order);
        }else{
            Asserts.fail("只能删除已完成或已关闭的订单！");
        }
    }

    @Override
    @Transactional
    public void paySuccessByOrderSn(String orderSn, Integer payType) {
        List<OmsOrder> orderList = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getOrderSn, orderSn)
                        .eq(OmsOrder::getStatus, 0)
                        .eq(OmsOrder::getDeleteStatus, 0));
        if(CollUtil.isNotEmpty(orderList)){
            OmsOrder order = orderList.get(0);
            paySuccess(order.getId(),payType);
        }
    }

    /**
     * 生成18位订单编号:8位日期+2位平台号码+2位支付方式+6位以上自增id
     */
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private String generateOrderSn(OmsOrder order) {
        StringBuilder sb = new StringBuilder();
        String date = LocalDate.now().format(ORDER_DATE_FORMATTER);
        String key = REDIS_DATABASE+":"+ REDIS_KEY_ORDER_ID + date;
        Long increment = redisService.incr(key, 1);
        sb.append(date);
        sb.append(String.format("%02d", order.getSourceType()));
        sb.append(String.format("%02d", order.getPayType()));
        String incrementStr = increment.toString();
        if (incrementStr.length() <= 6) {
            sb.append(String.format("%06d", increment));
        } else {
            sb.append(incrementStr);
        }
        return sb.toString();
    }

    /**
     * 删除下单商品的购物车信息
     */
    private void deleteCartItemList(List<CartPromotionItem> cartPromotionItemList, UmsMember currentMember) {
        List<Long> ids = new ArrayList<>();
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            ids.add(cartPromotionItem.getId());
        }
        cartItemService.delete(currentMember.getId(), ids);
    }

    /**
     * 计算该订单赠送的成长值（已修复空指针）
     */
    private Integer calcGiftGrowth(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            Integer giftGrowth = Objects.requireNonNullElse(orderItem.getGiftGrowth(), 0);
            sum = sum + giftGrowth * orderItem.getProductQuantity();
        }
        return sum;
    }

    /**
     * 计算该订单赠送的积分（已修复空指针）
     */
    private Integer calcGifIntegration(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            sum += Objects.requireNonNullElse(orderItem.getGiftIntegration(), 0) * orderItem.getProductQuantity();
        }
        return sum;
    }

    /**
     * 将优惠券信息更改为指定状态
     */
    private void updateCouponStatus(Long couponId, Long memberId, Integer useStatus) {
        updateCouponStatus(couponId, memberId, useStatus, null, null);
    }

    /**
     * 将优惠券信息更改为指定状态（带订单编号）
     */
    private void updateCouponStatus(Long couponId, Long memberId, Integer useStatus, Long orderId, String orderSn) {
        if (couponId == null) {
            return;
        }
        List<SmsCouponHistory> couponHistoryList = couponHistoryMapper.selectList(
                new LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getUseStatus, useStatus == 0 ? 1 : 0));
        if (!CollectionUtils.isEmpty(couponHistoryList)) {
            SmsCouponHistory couponHistory = couponHistoryList.get(0);
            if (useStatus == 1) {
                // 使用优惠券
                couponHistory.setUseTime(new Date());
                couponHistory.setUseStatus(useStatus);
                couponHistory.setOrderId(orderId);
                couponHistory.setOrderSn(orderSn);
            } else {
                // 取消使用，恢复为未使用状态，清除订单编号
                couponHistory.setUseStatus(useStatus);
                couponHistory.setOrderId(null);
                couponHistory.setOrderSn(null);
            }
            couponHistoryMapper.updateById(couponHistory);
            // 更新优惠券的使用数量统计
            updateCouponUseCount(couponId, useStatus);
        }
    }

    /**
     * 更新优惠券的使用数量统计
     */
    private void updateCouponUseCount(Long couponId, Integer useStatus) {
        SmsCoupon coupon = couponMapper.selectById(couponId);
        if (coupon != null) {
            if (useStatus == 1) {
                // 使用优惠券，useCount+1
                coupon.setUseCount(coupon.getUseCount() == null ? 1 : coupon.getUseCount() + 1);
            } else {
                // 取消使用，useCount-1
                coupon.setUseCount(coupon.getUseCount() == null ? 0 : Math.max(0, coupon.getUseCount() - 1));
            }
            couponMapper.updateById(coupon);
        }
    }

    private void handleRealAmount(List<OmsOrderItem> orderItemList) {
        for (OmsOrderItem orderItem : orderItemList) {
            //原价-促销优惠-优惠券抵扣-积分抵扣
            BigDecimal realAmount = orderItem.getProductPrice()
                    .subtract(orderItem.getPromotionAmount())
                    .subtract(orderItem.getCouponAmount())
                    .subtract(orderItem.getIntegrationAmount());
            orderItem.setRealAmount(realAmount);
        }
    }

    /**
     * 获取订单促销信息
     */
    private String getOrderPromotionInfo(List<OmsOrderItem> orderItemList) {
        StringBuilder sb = new StringBuilder();
        for (OmsOrderItem orderItem : orderItemList) {
            sb.append(orderItem.getPromotionName());
            sb.append(";");
        }
        String result = sb.toString();
        if (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 计算订单应付金额
     */
    private BigDecimal calcPayAmount(OmsOrder order) {
        //总金额+运费-促销优惠-优惠券优惠-积分抵扣
        return order.getTotalAmount()
                .add(order.getFreightAmount())
                .subtract(order.getPromotionAmount())
                .subtract(order.getCouponAmount())
                .subtract(order.getIntegrationAmount());
    }

    /**
     * 计算订单优惠券金额
     */
    private BigDecimal calcIntegrationAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal integrationAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getIntegrationAmount() != null) {
                integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return integrationAmount;
    }

    /**
     * 计算订单优惠券金额
     */
    private BigDecimal calcCouponAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal couponAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getCouponAmount() != null) {
                couponAmount = couponAmount.add(orderItem.getCouponAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return couponAmount;
    }

    /**
     * 计算订单活动优惠
     */
    private BigDecimal calcPromotionAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal promotionAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getPromotionAmount() != null) {
                promotionAmount = promotionAmount.add(orderItem.getPromotionAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return promotionAmount;
    }

    /**
     * 获取可用积分抵扣金额
     */
    private BigDecimal getUseIntegrationAmount(Integer useIntegration, BigDecimal totalAmount, UmsMember currentMember, boolean hasCoupon) {
        BigDecimal zeroAmount = new BigDecimal(0);
        //判断用户是否有这么多积分
        if (useIntegration.compareTo(currentMember.getIntegration()) > 0) {
            return zeroAmount;
        }
        //根据积分使用规则判断是否可用
        UmsIntegrationConsumeSetting integrationConsumeSetting = integrationConsumeSettingMapper.selectById(1L);
        if (integrationConsumeSetting == null) {
            return zeroAmount;
        }
        if (hasCoupon && integrationConsumeSetting.getCouponStatus().equals(0)) {
            //不可与优惠券共用
            return zeroAmount;
        }
        //是否达到最低使用积分门槛
        if (useIntegration.compareTo(integrationConsumeSetting.getUseUnit()) < 0) {
            return zeroAmount;
        }
        //是否超过订单抵用最高百分比
        BigDecimal integrationAmount = new BigDecimal(useIntegration).divide(new BigDecimal(integrationConsumeSetting.getUseUnit()), 2, RoundingMode.HALF_EVEN);
        BigDecimal maxPercent = new BigDecimal(integrationConsumeSetting.getMaxPercentPerOrder()).divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN);
        if (integrationAmount.compareTo(totalAmount.multiply(maxPercent)) > 0) {
            return zeroAmount;
        }
        return integrationAmount;
    }

    /**
     * 对优惠券优惠进行处理
     */
    private void handleCouponAmount(List<OmsOrderItem> orderItemList, SmsCouponHistoryDetail couponHistoryDetail) {
        SmsCoupon coupon = couponHistoryDetail.getCoupon();
        if (coupon == null) {
            return;
        }
        if (coupon.getUseType().equals(0)) {
            //全场通用
            calcPerCouponAmount(orderItemList, coupon);
        } else if (coupon.getUseType().equals(1)) {
            //指定分类
            List<OmsOrderItem> couponOrderItemList = getCouponOrderItemByRelation(couponHistoryDetail, orderItemList, 0);
            calcPerCouponAmount(couponOrderItemList, coupon);
        } else if (coupon.getUseType().equals(2)) {
            //指定商品
            List<OmsOrderItem> couponOrderItemList = getCouponOrderItemByRelation(couponHistoryDetail, orderItemList, 1);
            calcPerCouponAmount(couponOrderItemList, coupon);
        }
    }

    /**
     * 对每个下单商品进行优惠券金额分摊的计算
     */
    private void calcPerCouponAmount(List<OmsOrderItem> orderItemList, SmsCoupon coupon) {
        BigDecimal totalAmount = calcTotalAmount(orderItemList);
        for (OmsOrderItem orderItem : orderItemList) {
            //(商品价格/可用商品总价)*优惠券面额
            BigDecimal couponAmount = orderItem.getProductPrice().divide(totalAmount, 3, RoundingMode.HALF_EVEN).multiply(coupon.getAmount());
            orderItem.setCouponAmount(couponAmount);
        }
    }

    /**
     * 获取与优惠券有关系的下单商品
     */
    private List<OmsOrderItem> getCouponOrderItemByRelation(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList, int type) {
        List<OmsOrderItem> result = new ArrayList<>();
        if (type == 0) {
            List<Long> categoryIdList = new ArrayList<>();
            for (SmsCouponProductCategoryRelation productCategoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                categoryIdList.add(productCategoryRelation.getProductCategoryId());
            }
            for (OmsOrderItem orderItem : orderItemList) {
                if (categoryIdList.contains(orderItem.getProductCategoryId())) {
                    result.add(orderItem);
                } else {
                    orderItem.setCouponAmount(new BigDecimal(0));
                }
            }
        } else if (type == 1) {
            List<Long> productIdList = new ArrayList<>();
            for (SmsCouponProductRelation productRelation : couponHistoryDetail.getProductRelationList()) {
                productIdList.add(productRelation.getProductId());
            }
            for (OmsOrderItem orderItem : orderItemList) {
                if (productIdList.contains(orderItem.getProductId())) {
                    result.add(orderItem);
                } else {
                    orderItem.setCouponAmount(new BigDecimal(0));
                }
            }
        }
        return result;
    }

    /**
     * 获取该用户可以使用的优惠券
     */
    private SmsCouponHistoryDetail getUseCoupon(List<CartPromotionItem> cartPromotionItemList, Long couponId) {
        List<SmsCouponHistoryDetail> couponHistoryDetailList = memberCouponService.listCart(cartPromotionItemList, 1);
        for (SmsCouponHistoryDetail couponHistoryDetail : couponHistoryDetailList) {
            if (couponHistoryDetail.getCoupon().getId().equals(couponId)) {
                return couponHistoryDetail;
            }
        }
        return null;
    }

    /**
     * 计算总金额
     */
    private BigDecimal calcTotalAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsOrderItem item : orderItemList) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getProductQuantity())));
        }
        return totalAmount;
    }

    /**
     * 锁定下单商品的所有库存（原子操作，防止超卖）
     */
    private void lockStock(List<CartPromotionItem> cartPromotionItemList) {
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            int result = skuStockMapper.increaseLockStock(cartPromotionItem.getProductSkuId(), cartPromotionItem.getQuantity());
            if (result == 0) {
                Asserts.fail("库存不足，无法下单");
            }
        }
    }

    /**
     * 判断下单商品是否都有库存
     */
    private boolean hasStock(List<CartPromotionItem> cartPromotionItemList) {
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            if (cartPromotionItem.getRealStock()==null
                    ||cartPromotionItem.getRealStock() <= 0
                    || cartPromotionItem.getRealStock() < cartPromotionItem.getQuantity())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算购物车中商品的价格
     */
    private ConfirmOrderResult.CalcAmount calcCartAmount(List<CartPromotionItem> cartPromotionItemList) {
        ConfirmOrderResult.CalcAmount calcAmount = new ConfirmOrderResult.CalcAmount();
        calcAmount.setFreightAmount(new BigDecimal(0));
        BigDecimal totalAmount = new BigDecimal("0");
        BigDecimal promotionAmount = new BigDecimal("0");
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            totalAmount = totalAmount.add(cartPromotionItem.getPrice().multiply(new BigDecimal(cartPromotionItem.getQuantity())));
            promotionAmount = promotionAmount.add(cartPromotionItem.getReduceAmount().multiply(new BigDecimal(cartPromotionItem.getQuantity())));
        }
        calcAmount.setTotalAmount(totalAmount);
        calcAmount.setPromotionAmount(promotionAmount);
        calcAmount.setPayAmount(totalAmount.subtract(promotionAmount));
        return calcAmount;
    }

}