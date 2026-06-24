package com.su.mall.portal.domain.coupon;

import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.SmsCouponProductRelation;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 指定商品优惠券策略（useType=2）
 * 只有指定商品满足使用条件
 */
@Component
public class ProductCouponScopeStrategy implements CouponScopeStrategy {

    @Override
    public BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail couponHistoryDetail) {
        List<Long> productIds = extractProductIds(couponHistoryDetail);
        BigDecimal total = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            if (!productIds.contains(item.getProductId())) {
                continue;
            }
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    @Override
    public List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList) {
        List<Long> productIdList = extractProductIds(couponHistoryDetail);
        List<OmsOrderItem> result = new ArrayList<>();
        for (OmsOrderItem orderItem : orderItemList) {
            if (productIdList.contains(orderItem.getProductId())) {
                result.add(orderItem);
            } else {
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        }
        return result;
    }

    private List<Long> extractProductIds(SmsCouponHistoryDetail couponHistoryDetail) {
        List<Long> productIds = new ArrayList<>();
        if (couponHistoryDetail.getProductRelationList() != null) {
            for (SmsCouponProductRelation productRelation : couponHistoryDetail.getProductRelationList()) {
                productIds.add(productRelation.getProductId());
            }
        }
        return productIds;
    }
}