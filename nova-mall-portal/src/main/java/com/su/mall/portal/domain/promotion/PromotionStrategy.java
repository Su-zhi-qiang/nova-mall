package com.su.mall.portal.domain.promotion;

import com.su.mall.portal.domain.CartPromotionItem;

/**
 * 促销策略接口
 */
public interface PromotionStrategy {

    /**
     * 计算促销信息
     *
     * @param context 促销上下文，包含购物车项、促销商品信息等
     * @return 带有促销信息的购物车项
     */
    CartPromotionItem calculatePromotion(PromotionContext context);
}