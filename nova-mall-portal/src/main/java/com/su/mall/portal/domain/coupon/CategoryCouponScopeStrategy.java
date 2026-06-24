package com.su.mall.portal.domain.coupon;

import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.SmsCouponProductCategoryRelation;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 指定分类优惠券策略（useType=1）
 * 只有指定分类下的商品满足使用条件
 */
@Component
public class CategoryCouponScopeStrategy implements CouponScopeStrategy {

    @Override
    public BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail couponHistoryDetail) {
        List<Long> productCategoryIds = extractCategoryIds(couponHistoryDetail);
        BigDecimal total = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            if (!productCategoryIds.contains(item.getProductCategoryId())) {
                continue;
            }
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    @Override
    public List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList) {
        List<Long> categoryIdList = extractCategoryIds(couponHistoryDetail);
        List<OmsOrderItem> result = new ArrayList<>();
        for (OmsOrderItem orderItem : orderItemList) {
            if (categoryIdList.contains(orderItem.getProductCategoryId())) {
                result.add(orderItem);
            } else {
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        }
        return result;
    }

    private List<Long> extractCategoryIds(SmsCouponHistoryDetail couponHistoryDetail) {
        List<Long> productCategoryIds = new ArrayList<>();
        if (couponHistoryDetail.getCategoryRelationList() != null) {
            for (SmsCouponProductCategoryRelation categoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                productCategoryIds.add(categoryRelation.getProductCategoryId());
            }
        }
        return productCategoryIds;
    }
}