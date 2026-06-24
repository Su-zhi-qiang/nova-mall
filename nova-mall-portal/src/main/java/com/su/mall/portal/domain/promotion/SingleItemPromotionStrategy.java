package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 单品促销策略实现
 */
@Component
public class SingleItemPromotionStrategy implements PromotionStrategy {

    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);
        cartPromotionItem.setPromotionMessage("单品促销");

        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null;
        }
        BigDecimal originalPrice = skuStock.getPrice();
        cartPromotionItem.setPrice(originalPrice);
        cartPromotionItem.setReduceAmount(originalPrice.subtract(skuStock.getPromotionPrice()));
        Integer lockStockVal = skuStock.getLockStock();
        int lockStock = lockStockVal == null ? 0 : lockStockVal;
        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);
        cartPromotionItem.setIntegration(context.getPromotionProduct().getGiftPoint());
        cartPromotionItem.setGrowth(context.getPromotionProduct().getGiftGrowth());

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