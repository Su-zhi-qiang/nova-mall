package com.su.mall.portal.domain.coupon;

import com.su.mall.model.OmsOrderItem;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券适用范围策略接口（策略模式）
 * <p>根据优惠券的 useType 字段决定哪些商品可以使用该优惠券
 * <p>通过 {@link CouponScopeStrategyFactory} 工厂获取具体策略实例
 *
 * @see AllProductCouponScopeStrategy 全场通用（useType=0）
 * @see CategoryCouponScopeStrategy 指定分类（useType=1）
 * @see ProductCouponScopeStrategy 指定商品（useType=2）
 */
public interface CouponScopeStrategy {

    /**
     * 计算购物车中满足优惠券使用条件的商品总金额
     * <p>用于判断是否达到优惠券使用门槛（满减/满折的"满"金额）
     *
     * @param cartItemList         购物车促销商品列表
     * @param couponHistoryDetail  优惠券领取记录（包含关联商品/分类信息）
     * @return 满足条件的商品合计金额
     */
    BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail couponHistoryDetail);

    /**
     * 从订单商品列表中筛选满足优惠券条件的商品
     * <p>不满足条件的商品其couponAmount将被设为0
     *
     * @param couponHistoryDetail 优惠券记录（包含适用范围配置）
     * @param orderItemList       订单商品列表
     * @return 满足优惠券使用条件的商品列表
     */
    List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList);
}
