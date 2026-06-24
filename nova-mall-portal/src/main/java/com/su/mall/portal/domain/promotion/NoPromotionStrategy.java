package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 无优惠策略实现
 */
@Component
public class NoPromotionStrategy implements PromotionStrategy {

    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);
        cartPromotionItem.setPromotionMessage("无优惠");
        cartPromotionItem.setReduceAmount(new BigDecimal(0));

        PromotionProduct promotionProduct = context.getPromotionProduct();
        if (promotionProduct != null) {
            PmsSkuStock skuStock = getOriginalPrice(promotionProduct, context.getItem().getProductSkuId());
            if (skuStock != null) {
                Integer lockStockVal = skuStock.getLockStock();
                int lockStock = lockStockVal == null ? 0 : lockStockVal;
                cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);
            }
            cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
            cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
        }

        return cartPromotionItem;
    }

    private PmsSkuStock getOriginalPrice(PromotionProduct promotionProduct, Long productSkuId) {
        for (PmsSkuStock skuStock : promotionProduct.getSkuStockList()) {
            if (productSkuId.equals(skuStock.getId())) {
                return skuStock;
            }
        }
        return null;
    }
}