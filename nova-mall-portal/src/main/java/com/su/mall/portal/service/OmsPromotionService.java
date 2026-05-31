package com.su.mall.portal.service;

import com.su.mall.model.OmsCartItem;
import com.su.mall.portal.domain.CartPromotionItem;

import java.util.List;

/**
 * 促销管理Service
 * @author Su
 */
public interface OmsPromotionService {
    /**
     * 计算购物车中的促销活动信息
     * @param cartItemList 购物车
     */
    List<CartPromotionItem> calcCartPromotion(List<OmsCartItem> cartItemList);
}
