package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsProductFullReduction;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 满减优惠策略实现
 */
@Component
public class FullReductionPromotionStrategy implements PromotionStrategy {

    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        PmsProductFullReduction fullReduction = context.getFullReduction();
        if (fullReduction == null) {
            return null;
        }

        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);
        String message = getFullReductionPromotionMessage(fullReduction);
        cartPromotionItem.setPromotionMessage(message);

        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null;
        }
        BigDecimal originalPrice = skuStock.getPrice();
        BigDecimal totalAmount = context.getTotalAmount();
        BigDecimal reduceAmount = originalPrice.divide(totalAmount, RoundingMode.HALF_EVEN).multiply(fullReduction.getReducePrice());
        cartPromotionItem.setReduceAmount(reduceAmount);
        Integer lockStockVal = skuStock.getLockStock();
        int lockStock = lockStockVal == null ? 0 : lockStockVal;
        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);
        cartPromotionItem.setIntegration(context.getPromotionProduct().getGiftPoint());
        cartPromotionItem.setGrowth(context.getPromotionProduct().getGiftGrowth());

        return cartPromotionItem;
    }

    private String getFullReductionPromotionMessage(PmsProductFullReduction fullReduction) {
        StringBuilder sb = new StringBuilder();
        sb.append("满减优惠：");
        sb.append("满");
        sb.append(fullReduction.getFullPrice());
        sb.append("元，");
        sb.append("减");
        sb.append(fullReduction.getReducePrice());
        sb.append("元");
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