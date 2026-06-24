package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;
import com.su.mall.portal.domain.coupon.CouponScopeStrategy;
import com.su.mall.portal.domain.coupon.CouponScopeStrategyFactory;
import com.su.mall.portal.service.UmsMemberCouponService;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.SmsCoupon;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
public class HandleCouponHandler extends OrderHandler {

    private final UmsMemberCouponService memberCouponService;
    private final CouponScopeStrategyFactory couponScopeStrategyFactory;

    @Override
    public void handle(OrderHandlerContext context) {
        Long couponId = context.getAttribute("couponId");
        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();
        List<OmsOrderItem> orderItemList = context.getOrderItemList();

        if (couponId == null) {
            for (OmsOrderItem orderItem : orderItemList) {
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        } else {
            SmsCouponHistoryDetail couponHistoryDetail = getUseCoupon(cartPromotionItemList, couponId);
            if (couponHistoryDetail == null) {
                Asserts.fail("该优惠券不可用");
            }
            SmsCoupon coupon = couponHistoryDetail.getCoupon();
            if (coupon != null) {
                CouponScopeStrategy strategy = couponScopeStrategyFactory.getStrategy(coupon.getUseType());
                List<OmsOrderItem> couponOrderItemList = strategy.filterOrderItems(couponHistoryDetail, orderItemList);
                calcPerCouponAmount(couponOrderItemList, coupon);
            }
        }
        handleNext(context);
    }

    private SmsCouponHistoryDetail getUseCoupon(List<CartPromotionItem> cartPromotionItemList, Long couponId) {
        List<SmsCouponHistoryDetail> couponHistoryDetailList = memberCouponService.listCart(cartPromotionItemList, 1);
        for (SmsCouponHistoryDetail couponHistoryDetail : couponHistoryDetailList) {
            if (couponHistoryDetail.getCoupon().getId().equals(couponId)) {
                return couponHistoryDetail;
            }
        }
        return null;
    }

    private void calcPerCouponAmount(List<OmsOrderItem> orderItemList, SmsCoupon coupon) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OmsOrderItem orderItem : orderItemList) {
            totalAmount = totalAmount.add(orderItem.getProductPrice().multiply(new BigDecimal(orderItem.getProductQuantity())));
        }
        for (OmsOrderItem orderItem : orderItemList) {
            BigDecimal couponAmount = orderItem.getProductPrice().divide(totalAmount, 3, RoundingMode.HALF_EVEN).multiply(coupon.getAmount());
            orderItem.setCouponAmount(couponAmount);
        }
    }
}