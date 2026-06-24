package com.su.mall.portal;

import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import com.su.mall.portal.domain.promotion.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 促销策略模式单元测试
 */
public class PromotionStrategyTests {

    /**
     * 测试工厂获取策略
     */
    @Test
    public void testFactoryGetStrategy() {
        // 秒杀策略
        assertNotNull(PromotionStrategyFactory.getFlashStrategy());
        assertTrue(PromotionStrategyFactory.getFlashStrategy() instanceof FlashPromotionStrategy);

        // 单品促销策略
        PromotionStrategy singleStrategy = PromotionStrategyFactory.getStrategy(PromotionStrategyFactory.SINGLE_ITEM_PROMOTION_TYPE);
        assertTrue(singleStrategy instanceof SingleItemPromotionStrategy);

        // 打折策略
        PromotionStrategy ladderStrategy = PromotionStrategyFactory.getStrategy(PromotionStrategyFactory.LADDER_DISCOUNT_PROMOTION_TYPE);
        assertTrue(ladderStrategy instanceof LadderDiscountPromotionStrategy);

        // 满减策略
        PromotionStrategy fullReductionStrategy = PromotionStrategyFactory.getStrategy(PromotionStrategyFactory.FULL_REDUCTION_PROMOTION_TYPE);
        assertTrue(fullReductionStrategy instanceof FullReductionPromotionStrategy);

        // 无优惠策略
        PromotionStrategy noStrategy = PromotionStrategyFactory.getStrategy(PromotionStrategyFactory.NO_PROMOTION_TYPE);
        assertTrue(noStrategy instanceof NoPromotionStrategy);

        // null类型返回无优惠策略
        PromotionStrategy nullStrategy = PromotionStrategyFactory.getStrategy(null);
        assertTrue(nullStrategy instanceof NoPromotionStrategy);
    }

    /**
     * 测试无优惠策略
     */
    @Test
    public void testNoPromotionStrategy() {
        PromotionStrategy strategy = PromotionStrategyFactory.getStrategy(PromotionStrategyFactory.NO_PROMOTION_TYPE);

        com.su.mall.model.OmsCartItem cartItem = new com.su.mall.model.OmsCartItem();
        cartItem.setProductId(1L);
        cartItem.setProductSkuId(100L);
        cartItem.setQuantity(2);

        PromotionProduct promotionProduct = new PromotionProduct();
        promotionProduct.setId(1L);
        promotionProduct.setGiftPoint(10);
        promotionProduct.setGiftGrowth(5);

        com.su.mall.model.PmsSkuStock skuStock = new com.su.mall.model.PmsSkuStock();
        skuStock.setId(100L);
        skuStock.setPrice(new BigDecimal("99.99"));
        skuStock.setStock(100);
        skuStock.setLockStock(0);
        List<com.su.mall.model.PmsSkuStock> skuList = new ArrayList<>();
        skuList.add(skuStock);
        promotionProduct.setSkuStockList(skuList);

        PromotionContext context = PromotionContext.builder()
                .item(cartItem)
                .promotionProduct(promotionProduct)
                .build();

        CartPromotionItem result = strategy.calculatePromotion(context);

        assertNotNull(result);
        assertEquals("无优惠", result.getPromotionMessage());
        assertEquals(new BigDecimal("0"), result.getReduceAmount());
        assertEquals(100, result.getRealStock());
        assertEquals(10, result.getIntegration());
        assertEquals(5, result.getGrowth());
    }

    /**
     * 测试无优惠策略 - promotionProduct为null
     */
    @Test
    public void testNoPromotionStrategyWithNullProduct() {
        PromotionStrategy strategy = PromotionStrategyFactory.getStrategy(PromotionStrategyFactory.NO_PROMOTION_TYPE);

        com.su.mall.model.OmsCartItem cartItem = new com.su.mall.model.OmsCartItem();
        cartItem.setProductId(1L);
        cartItem.setProductSkuId(100L);
        cartItem.setQuantity(2);

        PromotionContext context = PromotionContext.builder()
                .item(cartItem)
                .promotionProduct(null)
                .build();

        CartPromotionItem result = strategy.calculatePromotion(context);

        assertNotNull(result);
        assertEquals("无优惠", result.getPromotionMessage());
        assertEquals(new BigDecimal("0"), result.getReduceAmount());
    }

    /**
     * 测试单品促销策略
     */
    @Test
    public void testSingleItemPromotionStrategy() {
        PromotionStrategy strategy = PromotionStrategyFactory.getStrategy(PromotionStrategyFactory.SINGLE_ITEM_PROMOTION_TYPE);

        com.su.mall.model.OmsCartItem cartItem = new com.su.mall.model.OmsCartItem();
        cartItem.setProductId(1L);
        cartItem.setProductSkuId(100L);
        cartItem.setQuantity(1);

        PromotionProduct promotionProduct = new PromotionProduct();
        promotionProduct.setId(1L);
        promotionProduct.setGiftPoint(10);
        promotionProduct.setGiftGrowth(5);

        com.su.mall.model.PmsSkuStock skuStock = new com.su.mall.model.PmsSkuStock();
        skuStock.setId(100L);
        skuStock.setPrice(new BigDecimal("100.00"));
        skuStock.setPromotionPrice(new BigDecimal("80.00"));
        skuStock.setStock(50);
        skuStock.setLockStock(5);
        List<com.su.mall.model.PmsSkuStock> skuList = new ArrayList<>();
        skuList.add(skuStock);
        promotionProduct.setSkuStockList(skuList);

        PromotionContext context = PromotionContext.builder()
                .item(cartItem)
                .promotionProduct(promotionProduct)
                .build();

        CartPromotionItem result = strategy.calculatePromotion(context);

        assertNotNull(result);
        assertEquals("单品促销", result.getPromotionMessage());
        assertEquals(new BigDecimal("20.00"), result.getReduceAmount());
        assertEquals(45, result.getRealStock());
    }
}