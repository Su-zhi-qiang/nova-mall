package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 秒杀促销策略实现
 */
@Component
public class FlashPromotionStrategy implements PromotionStrategy {

    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        if (context.getFlashRelation() == null) {
            return null;
        }

        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);
        cartPromotionItem.setPromotionMessage("限时秒杀");
        cartPromotionItem.setFlashPromotion(true);
        cartPromotionItem.setFlashPromotionId(context.getFlashRelation().getFlashPromotionId());
        cartPromotionItem.setFlashPromotionSessionId(context.getFlashRelation().getFlashPromotionSessionId());
        cartPromotionItem.setFlashPromotionRelationId(context.getFlashRelation().getId());

        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null;
        }
        BigDecimal originalPrice = skuStock.getPrice();
        cartPromotionItem.setPrice(originalPrice);
        cartPromotionItem.setReduceAmount(originalPrice.subtract(context.getFlashRelation().getFlashPromotionPrice()));
        Integer dailyStock = context.getDailyStock();
        int realStock = dailyStock != null ? dailyStock : 0;
        cartPromotionItem.setRealStock(realStock);
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