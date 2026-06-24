package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsProductLadder;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 打折优惠策略实现
 */
@Component
public class LadderDiscountPromotionStrategy implements PromotionStrategy {

    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        PmsProductLadder ladder = context.getLadder();
        if (ladder == null) {
            return null;
        }

        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);
        String message = getLadderPromotionMessage(ladder);
        cartPromotionItem.setPromotionMessage(message);

        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null;
        }
        BigDecimal originalPrice = skuStock.getPrice();
        BigDecimal reduceAmount = originalPrice.subtract(ladder.getDiscount().multiply(originalPrice));
        cartPromotionItem.setReduceAmount(reduceAmount);
        Integer lockStockVal = skuStock.getLockStock();
        int lockStock = lockStockVal == null ? 0 : lockStockVal;
        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);
        cartPromotionItem.setIntegration(context.getPromotionProduct().getGiftPoint());
        cartPromotionItem.setGrowth(context.getPromotionProduct().getGiftGrowth());

        return cartPromotionItem;
    }

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

    private PmsSkuStock getOriginalPrice(PromotionProduct promotionProduct, Long productSkuId) {
        for (PmsSkuStock skuStock : promotionProduct.getSkuStockList()) {
            if (productSkuId.equals(skuStock.getId())) {
                return skuStock;
            }
        }
        return null;
    }
}