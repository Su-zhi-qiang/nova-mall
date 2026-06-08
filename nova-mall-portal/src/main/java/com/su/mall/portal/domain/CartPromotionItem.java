package com.su.mall.portal.domain;

import com.su.mall.model.OmsCartItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 购物车中促销信息的封装
 * @author Su
 */
@Getter
@Setter
public class CartPromotionItem extends OmsCartItem{
    @Schema(title = "促销活动信息")
    private String promotionMessage;
    @Schema(title = "促销活动减去的金额，针对每个商品")
    private BigDecimal reduceAmount;
    @Schema(title = "剩余库存-锁定库存")
    private Integer realStock;
    @Schema(title = "购买商品赠送积分")
    private Integer integration;
    @Schema(title = "购买商品赠送成长值")
    private Integer growth;
    @Schema(title = "是否是秒杀商品")
    private Boolean flashPromotion = false;
    @Schema(title = "秒杀活动ID")
    private Long flashPromotionId;
    @Schema(title = "秒杀场次ID")
    private Long flashPromotionSessionId;
    @Schema(title = "秒杀关联ID")
    private Long flashPromotionRelationId;
}
