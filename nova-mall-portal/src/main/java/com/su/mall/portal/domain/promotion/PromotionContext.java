package com.su.mall.portal.domain.promotion;

import com.su.mall.model.OmsCartItem;
import com.su.mall.model.PmsProductFullReduction;
import com.su.mall.model.PmsProductLadder;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.portal.domain.PromotionProduct;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 促销上下文，封装促销计算所需的所有信息
 */
@Getter
@Builder
public class PromotionContext {

    /**
     * 购物车项
     */
    private OmsCartItem item;

    /**
     * 促销商品信息
     */
    private PromotionProduct promotionProduct;

    /**
     * 秒杀关联信息（可选）
     */
    private SmsFlashPromotionProductRelation flashRelation;

    /**
     * 秒杀当日库存（可选）
     */
    private Integer dailyStock;

    /**
     * 打折优惠策略（可选）
     */
    private PmsProductLadder ladder;

    /**
     * 满减优惠策略（可选）
     */
    private PmsProductFullReduction fullReduction;

    /**
     * 商品总价（用于满减计算，可选）
     */
    private BigDecimal totalAmount;
}