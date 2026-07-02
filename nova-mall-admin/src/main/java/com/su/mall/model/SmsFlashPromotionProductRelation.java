package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 限时购商品关系
 * 
 */
@Data
public class SmsFlashPromotionProductRelation implements Serializable {
    @Schema(title = "编号")
    private Long id;

    private Long flashPromotionId;

    @Schema(title = "编号")
    private Long flashPromotionSessionId;

    private Long productId;

    @Schema(title = "限时购价格")
    private BigDecimal flashPromotionPrice;

    @Schema(title = "限时购数量")
    private Integer flashPromotionCount;

    @Schema(title = "原始限时购数量（用于场次重置）")
    private Integer originalCount;

    @Schema(title = "每人限购数量")
    private Integer flashPromotionLimit;

    @Schema(title = "排序")
    private Integer sort;

    @Schema(title = "已抢购数量")
    private Integer flashPromotionSold;

    @Serial
    private static final long serialVersionUID = 1L;
}
