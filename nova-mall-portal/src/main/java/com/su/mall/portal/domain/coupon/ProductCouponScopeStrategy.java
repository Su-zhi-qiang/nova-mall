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
 * <p>只有商品ID匹配的商品满足使用条件
 * <p>通过优惠券关联的商品关系表（SmsCouponProductRelation）判断
 *
 * @see CouponScopeStrategy
 */
@Component
public class ProductCouponScopeStrategy implements CouponScopeStrategy {

    /**
     * 计算购物车中指定商品的合计金额
     *
     * @param cartItemList 购物车促销商品列表
     * @param couponHistoryDetail 优惠券记录（包含关联商品列表）
     * @return 指定商品的实付金额之和
     */
    @Override
    public BigDecimal calcEligibleAmount(List<CartPromotionItem> cartItemList, SmsCouponHistoryDetail couponHistoryDetail) {
        // 1. 从优惠券记录中提取关联的商品ID列表
        List<Long> productIds = extractProductIds(couponHistoryDetail);

        // 2. 初始化金额合计为0
        BigDecimal total = BigDecimal.ZERO;

        // 3. 遍历购物车中的商品
        for (CartPromotionItem item : cartItemList) {
            // 4. 判断商品ID是否在优惠券允许的商品列表中
            if (!productIds.contains(item.getProductId())) {
                continue; // 不属于指定商品，跳过
            }

            // 5. 计算该商品的实付金额 = 原价 - 促销优惠
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());

            // 6. 累加到总金额 = 实付单价 × 购买数量
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }

        return total;
    }

    /**
     * 筛选指定商品的订单项，不匹配的商品优惠金额置为0
     */
    @Override
    public List<OmsOrderItem> filterOrderItems(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList) {
        // 1. 提取优惠券关联的商品ID列表
        List<Long> productIdList = extractProductIds(couponHistoryDetail);

        // 2. 创建筛选结果列表
        List<OmsOrderItem> result = new ArrayList<>();

        // 3. 遍历订单商品，按商品ID筛选
        for (OmsOrderItem orderItem : orderItemList) {
            if (productIdList.contains(orderItem.getProductId())) {
                // 4. 商品匹配，保留该商品参与优惠
                result.add(orderItem);
            } else {
                // 5. 商品不匹配，优惠金额置为0
                orderItem.setCouponAmount(new BigDecimal(0));
            }
        }

        return result;
    }

    /**
     * 从优惠券历史记录中提取关联的商品ID列表
     */
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
