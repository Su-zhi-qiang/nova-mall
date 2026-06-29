package com.su.mall.portal.domain;

import com.su.mall.model.OmsCartItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 购物车促销商品信息
 * <p>继承购物车项基础信息，扩展促销相关的字段（优惠金额、积分、库存、秒杀信息）
 * <p>由各 {@link com.su.mall.portal.domain.promotion.PromotionStrategy} 实现类构建
 *
 * @see com.su.mall.portal.domain.promotion.PromotionStrategy#calculatePromotion
 */
@Getter
@Setter
public class CartPromotionItem extends OmsCartItem {

    @Schema(title = "促销活动描述信息（如：满减优惠：满100元，减20元）")
    private String promotionMessage;

    @Schema(title = "促销活动减免的金额（每个商品的优惠分摊）")
    private BigDecimal reduceAmount;

    @Schema(title = "实际可用库存（总库存 - 锁定库存）")
    private Integer realStock;

    @Schema(title = "购买商品赠送积分")
    private Integer integration;

    @Schema(title = "购买商品赠送成长值")
    private Integer growth;

    @Schema(title = "是否为秒杀商品")
    private Boolean flashPromotion = false;

    @Schema(title = "秒杀活动ID")
    private Long flashPromotionId;

    @Schema(title = "秒杀场次ID")
    private Long flashPromotionSessionId;

    @Schema(title = "秒杀关联ID")
    private Long flashPromotionRelationId;
}
