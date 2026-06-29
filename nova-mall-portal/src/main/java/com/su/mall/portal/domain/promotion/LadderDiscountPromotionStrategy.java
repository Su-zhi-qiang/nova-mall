package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsProductLadder;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 阶梯打折策略实现
 * <p>根据购买件数达到阶梯门槛后，按折扣比例计算优惠金额
 * <p>公式：优惠金额 = 原价 × (1 - 折扣系数)，其中折扣系数为小数（如0.8表示8折）
 * <p>规则示例：满2件打8折、满3件打7折
 *
 * @see PromotionStrategy
 */
@Component
public class LadderDiscountPromotionStrategy implements PromotionStrategy {

    /**
     * 计算阶梯打折优惠金额
     * <p>核心算法：优惠金额 = 原价 - (原价 × 折扣系数)
     * <p>折扣系数为BigDecimal小数形式，如8折对应0.8，7折对应0.7
     *
     * @param context 促销计算上下文，需包含阶梯打折规则和商品信息
     * @return 包含优惠信息的购物车促销商品，无打折规则或SKU不存在时返回null
     */
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        // 1. 获取阶梯打折优惠规则，无规则则不参与打折
        PmsProductLadder ladder = context.getLadder();
        if (ladder == null) {
            return null;
        }

        // 2. 创建促销商品对象，复制购物车项的基础属性
        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);

        // 3. 设置促销活动描述信息（如："打折优惠：满2件，打8折"）
        cartPromotionItem.setPromotionMessage(getLadderPromotionMessage(ladder));

        // 4. 根据SKU ID查找对应的商品库存信息（获取原价）
        PmsSkuStock skuStock = getSkuStock(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null; // 未找到对应SKU，无法计算
        }

        // 5. 获取商品原价
        BigDecimal originalPrice = skuStock.getPrice();

        // 6. 计算打折优惠金额 = 原价 - (原价 × 折扣系数)
        BigDecimal reduceAmount = originalPrice.subtract(ladder.getDiscount().multiply(originalPrice));
        cartPromotionItem.setReduceAmount(reduceAmount);

        // 7. 计算实际可用库存 = 总库存 - 锁定库存
        Integer lockStockVal = skuStock.getLockStock();
        int lockStock = lockStockVal == null ? 0 : lockStockVal;
        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);

        // 8. 设置购买赠送的积分和成长值
        cartPromotionItem.setIntegration(context.getPromotionProduct().getGiftPoint());
        cartPromotionItem.setGrowth(context.getPromotionProduct().getGiftGrowth());

        return cartPromotionItem;
    }

    /**
     * 构建阶梯打折促销描述信息
     *
     * @param ladder 阶梯打折规则配置
     * @return 格式化的促销描述，如："打折优惠：满2件，打8折"
     */
    private String getLadderPromotionMessage(PmsProductLadder ladder) {
        StringBuilder sb = new StringBuilder();
        sb.append("打折优惠：");
        sb.append("满");
        sb.append(ladder.getCount());
        sb.append("件，");
        sb.append("打");
        sb.append(ladder.getDiscount().multiply(new BigDecimal(10)));
        sb.append("折");
        return sb.toString();
    }

    /**
     * 根据SKU ID查找对应的库存信息
     *
     * @param promotionProduct 促销商品信息（包含SKU库存列表）
     * @param productSkuId     商品SKU ID
     * @return 匹配的SKU库存信息，未找到返回null
     */
    private PmsSkuStock getSkuStock(PromotionProduct promotionProduct, Long productSkuId) {
        for (PmsSkuStock skuStock : promotionProduct.getSkuStockList()) {
            if (productSkuId.equals(skuStock.getId())) {
                return skuStock;
            }
        }
        return null;
    }
}
