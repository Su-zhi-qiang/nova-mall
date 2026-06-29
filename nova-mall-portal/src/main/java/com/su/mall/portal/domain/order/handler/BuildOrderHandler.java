package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.OmsOrderSetting;
import com.su.mall.mapper.OmsOrderSettingMapper;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 订单创建链 - 第10步：构建OmsOrder订单对象
 * <p>汇总所有金额（总价、运费、促销、优惠券、积分、实付）→ 生成唯一订单号 → 设置收货信息和状态
 * <p>订单号格式：日期(8位) + 来源类型(2位) + 支付方式(2位) + 自增序号(6位)
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class BuildOrderHandler extends OrderHandler {

    private final OmsOrderSettingMapper orderSettingMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        List<OmsOrderItem> orderItemList = context.getOrderItemList();
        OmsOrder order = context.getOrder();
        Integer useIntegration = context.getAttribute("useIntegration");
        Long couponId = context.getAttribute("couponId");
        boolean hasFlashPromotion = context.isHasFlashPromotion();

        // 1. 设置订单金额信息
        order.setDiscountAmount(new BigDecimal(0));                          // 折扣金额（暂未使用）
        order.setTotalAmount(calcTotalAmount(orderItemList));                // 商品总价
        order.setFreightAmount(new BigDecimal(0));                           // 运费（暂免运费）
        order.setPromotionAmount(calcPromotionAmount(orderItemList));        // 促销优惠总额
        order.setPromotionInfo(getOrderPromotionInfo(orderItemList));        // 促销信息描述

        // 2. 设置优惠券信息
        if (couponId == null) {
            order.setCouponAmount(new BigDecimal(0));
        } else {
            order.setCouponId(couponId);
            order.setCouponAmount(calcCouponAmount(orderItemList));          // 优惠券抵扣总额
        }

        // 3. 设置积分信息
        if (useIntegration == null) {
            order.setIntegration(0);
            order.setIntegrationAmount(new BigDecimal(0));
        } else {
            order.setIntegration(useIntegration);
            order.setIntegrationAmount(calcIntegrationAmount(orderItemList)); // 积分抵扣总额
        }

        // 4. 计算实付金额 = 总价 + 运费 - 促销 - 优惠券 - 积分
        order.setPayAmount(calcPayAmount(order));

        // 5. 设置会员和时间信息
        order.setMemberId(context.getCurrentMember().getId());
        order.setCreateTime(new Date());
        order.setMemberUsername(context.getCurrentMember().getUsername());
        order.setPayType(context.getAttribute("payType"));
        order.setSourceType(1);                                              // 来源：PC端
        order.setStatus(0);                                                  // 状态：待付款

        // 6. 设置订单类型（1=秒杀订单，0=普通订单）
        order.setOrderType(hasFlashPromotion ? 1 : 0);

        // 7. 设置收货地址信息
        order.setReceiverName(context.getAttribute("receiverName"));
        order.setReceiverPhone(context.getAttribute("receiverPhone"));
        order.setReceiverPostCode(context.getAttribute("receiverPostCode"));
        order.setReceiverProvince(context.getAttribute("receiverProvince"));
        order.setReceiverCity(context.getAttribute("receiverCity"));
        order.setReceiverRegion(context.getAttribute("receiverRegion"));
        order.setReceiverDetailAddress(context.getAttribute("receiverDetailAddress"));

        // 8. 设置默认状态
        order.setConfirmStatus(0);                                           // 确认状态：未确认
        order.setDeleteStatus(0);                                            // 删除状态：未删除

        // 9. 计算赠送积分和成长值
        order.setIntegration(calcGifIntegration(orderItemList));
        order.setGrowth(calcGiftGrowth(orderItemList));

        // 10. 生成唯一订单号：日期 + 来源类型 + 支付方式 + Redis自增序号
        com.su.mall.common.service.RedisService redisService = context.getAttribute("redisService");
        String REDIS_KEY_ORDER_ID = context.getAttribute("REDIS_KEY_ORDER_ID");
        String REDIS_DATABASE = context.getAttribute("REDIS_DATABASE");
        StringBuilder sb = new StringBuilder();
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
        Long increment = redisService.incr(key, 1);                          // Redis原子自增生成序号
        sb.append(date);
        sb.append(String.format("%02d", order.getSourceType()));
        sb.append(String.format("%02d", order.getPayType()));
        String incrementStr = increment.toString();
        if (incrementStr.length() <= 6) {
            sb.append(String.format("%06d", increment));                     // 序号不足6位补零
        } else {
            sb.append(incrementStr);
        }
        order.setOrderSn(sb.toString());

        // 11. 获取自动确认收货天数配置
        List<OmsOrderSetting> orderSettings = orderSettingMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>());
        if (CollUtil.isNotEmpty(orderSettings)) {
            order.setAutoConfirmDay(orderSettings.get(0).getConfirmOvertime());
        }

        // 12. 传递给下一个处理器
        handleNext(context);
    }

    /** 计算商品总价 = Σ(单价 × 数量) */
    private BigDecimal calcTotalAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsOrderItem item : orderItemList) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getProductQuantity())));
        }
        return totalAmount;
    }

    /** 计算促销优惠总额 = Σ(促销金额 × 数量) */
    private BigDecimal calcPromotionAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal promotionAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getPromotionAmount() != null) {
                promotionAmount = promotionAmount.add(orderItem.getPromotionAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return promotionAmount;
    }

    /** 拼接促销信息描述（如："满减优惠：满100元，减20元;单品促销"） */
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

    /** 计算优惠券抵扣总额 = Σ(优惠券金额 × 数量) */
    private BigDecimal calcCouponAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal couponAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getCouponAmount() != null) {
                couponAmount = couponAmount.add(orderItem.getCouponAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return couponAmount;
    }

    /** 计算积分抵扣总额 = Σ(积分金额 × 数量) */
    private BigDecimal calcIntegrationAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal integrationAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getIntegrationAmount() != null) {
                integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return integrationAmount;
    }

    /** 计算实付金额 = 总价 + 运费 - 促销 - 优惠券 - 积分 */
    private BigDecimal calcPayAmount(OmsOrder order) {
        return order.getTotalAmount()
                .add(order.getFreightAmount())
                .subtract(order.getPromotionAmount())
                .subtract(order.getCouponAmount())
                .subtract(order.getIntegrationAmount());
    }

    /** 计算赠送积分总额 = Σ(赠送积分 × 数量) */
    private Integer calcGifIntegration(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            sum += Objects.requireNonNullElse(orderItem.getGiftIntegration(), 0) * orderItem.getProductQuantity();
        }
        return sum;
    }

    /** 计算赠送成长值总额 = Σ(赠送成长值 × 数量) */
    private Integer calcGiftGrowth(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            Integer giftGrowth = Objects.requireNonNullElse(orderItem.getGiftGrowth(), 0);
            sum = sum + giftGrowth * orderItem.getProductQuantity();
        }
        return sum;
    }
}
