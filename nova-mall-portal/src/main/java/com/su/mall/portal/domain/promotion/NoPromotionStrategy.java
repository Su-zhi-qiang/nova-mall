package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 无优惠策略实现（默认兜底策略）
 * <p>当商品未参与任何促销活动时使用，优惠金额固定为0
 * <p>由 {@link PromotionStrategyFactory} 在促销类型未匹配时作为默认策略返回
 *
 * @see PromotionStrategy
 */
@Component
public class NoPromotionStrategy implements PromotionStrategy {

    /**
     * 计算无优惠的促销信息
     * <p>直接复制购物车项信息，优惠金额设为0，库存按实际可用库存计算
     *
     * @param context 促销计算上下文
     * @return 包含零优惠信息的购物车促销商品
     */
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        // 1. 创建促销商品对象，复制购物车项的基础属性
        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);

        // 2. 设置无促销标识和描述信息
        cartPromotionItem.setPromotionMessage("无优惠");

        // 3. 无优惠策略，减免金额固定为0
        cartPromotionItem.setReduceAmount(new BigDecimal(0));

        // 4. 获取促销商品信息（可能为空）
        PromotionProduct promotionProduct = context.getPromotionProduct();
        if (promotionProduct != null) {
            // 5. 根据SKU ID查找对应的商品库存信息
            PmsSkuStock skuStock = getSkuStock(promotionProduct, context.getItem().getProductSkuId());
            if (skuStock != null) {
                // 6. 计算实际可用库存 = 总库存 - 锁定库存
                Integer lockStockVal = skuStock.getLockStock();
                int lockStock = lockStockVal == null ? 0 : lockStockVal;
                cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);
            }

            // 7. 设置购买赠送的积分和成长值
            cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
            cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
        }

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
