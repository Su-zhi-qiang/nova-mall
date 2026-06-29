package com.su.mall.portal.service;

import com.su.mall.model.OmsCartItem;
import com.su.mall.portal.domain.CartPromotionItem;

import java.util.List;

/**
 * 促销计算Service
 * <p>负责计算购物车中每个商品的促销优惠信息
 * <p>内部使用策略模式（{@link com.su.mall.portal.domain.promotion.PromotionStrategy}）根据促销类型分发计算
 *
 * @see com.su.mall.portal.domain.promotion.PromotionStrategyFactory
 */
public interface OmsPromotionService {

    /**
     * 计算购物车中所有商品的促销信息
     *
     * @param cartItemList 购物车商品列表
     * @return 包含促销优惠信息的购物车商品列表
     */
    List<CartPromotionItem> calcCartPromotion(List<OmsCartItem> cartItemList);
}
