package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 秒杀促销策略实现
 * <p>针对限时秒杀活动商品，使用秒杀专属价格替代原价
 * <p>优惠金额 = 原价 - 秒杀价，库存使用每日快照库存而非商品总库存
 *
 * @see PromotionStrategy
 * @see PromotionContext#getFlashRelation()
 */
@Component
public class FlashPromotionStrategy implements PromotionStrategy {

    /**
     * 计算秒杀促销优惠信息
     * <p>核心逻辑：将秒杀关联信息、秒杀价格、每日限量库存封装到促销商品中
     * <p>与普通促销不同，秒杀库存使用每日快照（{@link PromotionContext#getDailyStock()}）
     *
     * @param context 促销上下文，需包含秒杀关联信息（flashRelation）和每日库存（dailyStock）
     * @return 包含秒杀价格和库存的促销商品，无秒杀关联时返回null
     */
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        // 1. 获取秒杀活动关联信息，无关联则不参与秒杀
        if (context.getFlashRelation() == null) {
            return null;
        }

        // 2. 创建促销商品对象，复制购物车项的基础属性（商品ID、数量、SKU等）
        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);

        // 3. 设置秒杀促销标识和描述信息
        cartPromotionItem.setPromotionMessage("限时秒杀");
        cartPromotionItem.setFlashPromotion(true);

        // 4. 回传秒杀关联ID，下单时需用于校验和扣减秒杀库存
        cartPromotionItem.setFlashPromotionId(context.getFlashRelation().getFlashPromotionId());
        cartPromotionItem.setFlashPromotionSessionId(context.getFlashRelation().getFlashPromotionSessionId());
        cartPromotionItem.setFlashPromotionRelationId(context.getFlashRelation().getId());

        // 5. 根据SKU ID查找对应的商品库存信息（获取原价）
        PmsSkuStock skuStock = getSkuStock(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null; // 未找到对应SKU，无法计算
        }

        // 6. 设置商品原价，用于前端展示"原价划线 + 秒杀价"效果
        BigDecimal originalPrice = skuStock.getPrice();
        cartPromotionItem.setPrice(originalPrice);

        // 7. 计算秒杀优惠金额 = 原价 - 秒杀价
        cartPromotionItem.setReduceAmount(originalPrice.subtract(context.getFlashRelation().getFlashPromotionPrice()));

        // 8. 设置秒杀剩余库存（使用每日快照库存，而非商品总库存）
        Integer dailyStock = context.getDailyStock();
        int realStock = dailyStock != null ? dailyStock : 0;
        cartPromotionItem.setRealStock(realStock);

        // 9. 设置购买赠送的积分和成长值
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
