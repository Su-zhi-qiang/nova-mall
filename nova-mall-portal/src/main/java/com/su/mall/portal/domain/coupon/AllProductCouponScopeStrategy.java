package com.su.mall.portal.domain.coupon;

import com.su.mall.model.OmsOrderItem;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 全场通用优惠券策略（useType=0）
 * <p>所有商品均满足使用条件，无需额外筛选
 * <p>计算金额时直接累加购物车中所有商品的实付金额（原价 - 促销优惠）
 *
 * @see CouponScopeStrategy
 */
@Component
public class AllProductCouponScopeStrategy implements CouponScopeStrategy {

    /**
     * 计算购物车全部商品的实付金额合计
     *
     * @param cartItemList 购物车促销商品列表
     * @param couponHistoryDetail 优惠券记录（全场通用无需使用）
     * @return 全部商品实付金额之和
     */
    @Override
    public BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail couponHistoryDetail) {
        // 1. 初始化金额合计为0
        BigDecimal total = BigDecimal.ZERO;

        // 2. 遍历购物车中所有商品，累加实付金额
        for (CartPromotionItem item : cartItemList) {
            // 3. 计算单个商品的实付金额 = 原价 - 促销优惠金额
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());

            // 4. 累加到总金额 = 实付单价 × 购买数量
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }

        return total;
    }

    /**
     * 全场通用无需筛选，直接返回全部订单商品
     */
    @Override
    public List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList) {
        return orderItemList;
    }
}
