package com.su.mall.portal.domain.coupon;

import com.su.mall.model.OmsOrderItem;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券适用范围策略接口
 * 根据 useType 决定哪些商品可以使用优惠券
 */
public interface CouponScopeStrategy {

    /**
     * 计算购物车中满足优惠券使用条件的商品总金额
     */
    BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail couponHistoryDetail);

    /**
     * 从订单商品列表中筛选出满足优惠券使用条件的商品，并将不满足条件的商品优惠券金额设为0
     */
    List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList);
}