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
 * 促销计算上下文（Context对象）
 * <p>采用建造者模式封装促销计算所需的全部输入参数，供各 {@link PromotionStrategy} 实现类使用
 * <p>包含购物车项、商品信息、各类促销规则（秒杀/满减/打折）及订单总金额
 *
 * @see PromotionStrategy#calculatePromotion(PromotionContext)
 */
@Getter
@Builder
public class PromotionContext {

    /**
     * 购物车项（必填）
     * <p>包含商品ID、SKU ID、购买数量等基础购物车数据
     */
    private OmsCartItem item;

    /**
     * 促销商品信息（必填）
     * <p>包含商品的SKU库存列表、赠送积分、赠送成长值等促销相关数据
     */
    private PromotionProduct promotionProduct;

    /**
     * 秒杀活动关联信息（可选）
     * <p>仅在秒杀促销场景下非空，包含秒杀价格、秒杀限购数量等
     */
    private SmsFlashPromotionProductRelation flashRelation;

    /**
     * 秒杀活动当日库存（可选）
     * <p>秒杀活动的每日限量库存，用于判断是否超出限购数量
     */
    private Integer dailyStock;

    /**
     * 阶梯打折优惠规则（可选）
     * <p>仅在打折促销场景下非空，包含满件数、折扣比例等
     *
     * @see PmsProductLadder
     */
    private PmsProductLadder ladder;

    /**
     * 满减优惠规则（可选）
     * <p>仅在满减促销场景下非空，包含满额门槛、减免金额等
     *
     * @see PmsProductFullReduction
     */
    private PmsProductFullReduction fullReduction;

    /**
     * 订单中参与满减的商品总金额（可选）
     * <p>用于满减优惠的按比例分摊计算
     */
    private BigDecimal totalAmount;
}
