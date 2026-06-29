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
 * <p>只有商品分类匹配的商品满足使用条件
 * <p>通过优惠券关联的分类关系表（SmsCouponProductCategoryRelation）判断
 *
 * @see CouponScopeStrategy
 */
@Component
public class CategoryCouponScopeStrategy implements CouponScopeStrategy {

    /**
     * 计算购物车中属于指定分类的商品合计金额
     *
     * @param cartItemList 购物车促销商品列表
     * @param couponHistoryDetail 优惠券记录（包含关联分类列表）
     * @return 属于指定分类的商品实付金额之和
     */
    @Override
    public BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail couponHistoryDetail) {
        // 1. 从优惠券记录中提取关联的分类ID列表
        List<Long> productCategoryIds = extractCategoryIds(couponHistoryDetail);

        // 2. 初始化金额合计为0
        BigDecimal total = BigDecimal.ZERO;

        // 3. 遍历购物车中的商品
        for (CartPromotionItem item : cartItemList) {
            // 4. 判断商品分类是否在优惠券允许的分类列表中
            if (!productCategoryIds.contains(item.getProductCategoryId())) {
                continue; // 不属于指定分类，跳过
            }

            // 5. 计算该商品的实付金额 = 原价 - 促销优惠
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());

            // 6. 累加到总金额 = 实付单价 × 购买数量
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }

        return total;
    }

    /**
     * 筛选属于指定分类的订单商品，不匹配的商品优惠金额置为0
     */
    @Override
    public List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList) {
        // 1. 提取优惠券关联的分类ID列表
        List<Long> categoryIdList = extractCategoryIds(couponHistoryDetail);

        // 2. 创建筛选结果列表
        List<OmsOrderItem> result = new ArrayList<>();

        // 3. 遍历订单商品，按分类筛选
        for (OmsOrderItem orderItem : orderItemList) {
            if (categoryIdList.contains(orderItem.getProductCategoryId())) {
                // 4. 分类匹配，保留该商品参与优惠
                result.add(orderItem);
            } else {
                // 5. 分类不匹配，优惠金额置为0
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        }

        return result;
    }

    /**
     * 从优惠券历史记录中提取关联的分类ID列表
     */
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
