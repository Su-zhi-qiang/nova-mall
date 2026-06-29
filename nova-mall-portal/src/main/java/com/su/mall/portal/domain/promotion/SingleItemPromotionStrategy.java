package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 单品促销策略实现
 * <p>针对单个商品直接设定促销价格，优惠金额 = 原价 - 促销价
 * <p>适用于商品级别的直降促销场景
 *
 * @see PromotionStrategy
 */
@Component
public class SingleItemPromotionStrategy implements PromotionStrategy {

    /**
     * 计算单品促销优惠金额
     * <p>核心算法：直接使用SKU表中的促销价，优惠金额 = 原价 - 促销价
     *
     * @param context 促销计算上下文，需包含购物车项和促销商品信息
     * @return 包含优惠信息的购物车促销商品，无促销价或SKU不存在时返回null
     */
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        // 1. 创建促销商品对象，复制购物车项的基础属性（商品ID、数量、SKU等）
        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);

        // 2. 设置促销活动描述信息
        cartPromotionItem.setPromotionMessage("单品促销");

        // 3. 根据SKU ID查找对应的商品库存信息（包含原价和促销价）
        PmsSkuStock skuStock = getSkuStock(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null; // 未找到对应SKU，无法计算
        }

        // 4. 获取商品原价并设置到促销商品中
        BigDecimal originalPrice = skuStock.getPrice();
        cartPromotionItem.setPrice(originalPrice);

        // 5. 计算单品促销优惠金额 = 原价 - 促销价
        cartPromotionItem.setReduceAmount(originalPrice.subtract(skuStock.getPromotionPrice()));

        // 6. 计算实际可用库存 = 总库存 - 锁定库存
        Integer lockStockVal = skuStock.getLockStock();
        int lockStock = lockStockVal == null ? 0 : lockStockVal;
        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);

        // 7. 设置购买赠送的积分和成长值
        cartPromotionItem.setIntegration(context.getPromotionProduct().getGiftPoint());
        cartPromotionItem.setGrowth(context.getPromotionProduct().getGiftGrowth());

        return cartPromotionItem;
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
