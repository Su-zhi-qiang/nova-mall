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

        order.setDiscountAmount(new BigDecimal(0));
        order.setTotalAmount(calcTotalAmount(orderItemList));
        order.setFreightAmount(new BigDecimal(0));
        order.setPromotionAmount(calcPromotionAmount(orderItemList));
        order.setPromotionInfo(getOrderPromotionInfo(orderItemList));

        if (couponId == null) {
            order.setCouponAmount(new BigDecimal(0));
        } else {
            order.setCouponId(couponId);
            order.setCouponAmount(calcCouponAmount(orderItemList));
        }

        if (useIntegration == null) {
            order.setIntegration(0);
            order.setIntegrationAmount(new BigDecimal(0));
        } else {
            order.setIntegration(useIntegration);
            order.setIntegrationAmount(calcIntegrationAmount(orderItemList));
        }

        order.setPayAmount(calcPayAmount(order));
        order.setMemberId(context.getCurrentMember().getId());
        order.setCreateTime(new Date());
        order.setMemberUsername(context.getCurrentMember().getUsername());
        order.setPayType(context.getAttribute("payType"));
        order.setSourceType(1);
        order.setStatus(0);
        order.setOrderType(hasFlashPromotion ? 1 : 0);

        order.setReceiverName(context.getAttribute("receiverName"));
        order.setReceiverPhone(context.getAttribute("receiverPhone"));
        order.setReceiverPostCode(context.getAttribute("receiverPostCode"));
        order.setReceiverProvince(context.getAttribute("receiverProvince"));
        order.setReceiverCity(context.getAttribute("receiverCity"));
        order.setReceiverRegion(context.getAttribute("receiverRegion"));
        order.setReceiverDetailAddress(context.getAttribute("receiverDetailAddress"));

        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        order.setIntegration(calcGifIntegration(orderItemList));
        order.setGrowth(calcGiftGrowth(orderItemList));

        com.su.mall.common.service.RedisService redisService = context.getAttribute("redisService");
        String REDIS_KEY_ORDER_ID = context.getAttribute("REDIS_KEY_ORDER_ID");
        String REDIS_DATABASE = context.getAttribute("REDIS_DATABASE");
        StringBuilder sb = new StringBuilder();
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
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
        order.setOrderSn(sb.toString());

        List<OmsOrderSetting> orderSettings = orderSettingMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>());
        if (CollUtil.isNotEmpty(orderSettings)) {
            order.setAutoConfirmDay(orderSettings.get(0).getConfirmOvertime());
        }

        handleNext(context);
    }

    private BigDecimal calcTotalAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsOrderItem item : orderItemList) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getProductQuantity())));
        }
        return totalAmount;
    }

    private BigDecimal calcPromotionAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal promotionAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getPromotionAmount() != null) {
                promotionAmount = promotionAmount.add(orderItem.getPromotionAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return promotionAmount;
    }

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

    private BigDecimal calcCouponAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal couponAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getCouponAmount() != null) {
                couponAmount = couponAmount.add(orderItem.getCouponAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return couponAmount;
    }

    private BigDecimal calcIntegrationAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal integrationAmount = new BigDecimal(0);
        for (OmsOrderItem orderItem : orderItemList) {
            if (orderItem.getIntegrationAmount() != null) {
                integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
            }
        }
        return integrationAmount;
    }

    private BigDecimal calcPayAmount(OmsOrder order) {
        return order.getTotalAmount()
                .add(order.getFreightAmount())
                .subtract(order.getPromotionAmount())
                .subtract(order.getCouponAmount())
                .subtract(order.getIntegrationAmount());
    }

    private Integer calcGifIntegration(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            sum += Objects.requireNonNullElse(orderItem.getGiftIntegration(), 0) * orderItem.getProductQuantity();
        }
        return sum;
    }

    private Integer calcGiftGrowth(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            Integer giftGrowth = Objects.requireNonNullElse(orderItem.getGiftGrowth(), 0);
            sum = sum + giftGrowth * orderItem.getProductQuantity();
        }
        return sum;
    }
}