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

/**
 * 订单创建链 - 第6步：处理优惠券抵扣
 * <p>根据优惠券ID获取可用优惠券 → 通过策略工厂筛选适用商品 → 按比例分摊优惠金额
 * <p>分摊算法：couponAmount = (商品单价 / 适用商品总价) × 优惠券面额
 *
 * @see OrderHandler
 * @see CouponScopeStrategyFactory
 */
@RequiredArgsConstructor
public class HandleCouponHandler extends OrderHandler {

    private final UmsMemberCouponService memberCouponService;
    private final CouponScopeStrategyFactory couponScopeStrategyFactory;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取优惠券ID（可能为null，表示不使用优惠券）
        Long couponId = context.getAttribute("couponId");
        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();
        List<OmsOrderItem> orderItemList = context.getOrderItemList();

        // 2. 未使用优惠券时，所有订单项的优惠券金额设为0
        if (couponId == null) {
            for (OmsOrderItem orderItem : orderItemList) {
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        } else {
            // 3. 从用户可用优惠券中查找指定的优惠券
            SmsCouponHistoryDetail couponHistoryDetail = getUseCoupon(cartPromotionItemList, couponId);
            if (couponHistoryDetail == null) {
                Asserts.fail("该优惠券不可用");
            }

            SmsCoupon coupon = couponHistoryDetail.getCoupon();
            if (coupon != null) {
                // 4. 根据优惠券使用类型（全场/分类/商品）获取对应的适用范围策略
                CouponScopeStrategy strategy = couponScopeStrategyFactory.getStrategy(coupon.getUseType());

                // 5. 使用策略筛选出满足优惠券条件的订单商品
                List<OmsOrderItem> couponOrderItemList = strategy.filterOrderItems(couponHistoryDetail, orderItemList);

                // 6. 按比例分摊优惠金额到每个订单商品
                calcPerCouponAmount(couponOrderItemList, coupon);
            }
        }

        // 7. 传递给下一个处理器
        handleNext(context);
    }

    /**
     * 从用户可用优惠券中查找指定ID的优惠券
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
     * 按比例分摊优惠券金额到每个订单商品
     * <p>公式：单个商品优惠金额 = (商品单价 / 适用商品总价) × 优惠券面额
     * <p>精度：BigDecimal除法，RoundingMode.HALF_EVEN，保留3位小数
     */
    private void calcPerCouponAmount(List<OmsOrderItem> orderItemList, SmsCoupon coupon) {
        // 1. 计算适用商品的总价 = Σ(单价 × 数量)
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OmsOrderItem orderItem : orderItemList) {
            totalAmount = totalAmount.add(orderItem.getProductPrice().multiply(new BigDecimal(orderItem.getProductQuantity())));
        }

        // 2. 按比例分摊优惠金额到每个订单商品
        for (OmsOrderItem orderItem : orderItemList) {
            BigDecimal couponAmount = orderItem.getProductPrice()
                    .divide(totalAmount, 3, RoundingMode.HALF_EVEN)
                    .multiply(coupon.getAmount());
            orderItem.setCouponAmount(couponAmount);
        }
    }
}
