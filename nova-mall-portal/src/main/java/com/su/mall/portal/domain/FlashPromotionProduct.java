package com.su.mall.portal.domain;

import com.su.mall.model.PmsProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 秒杀信息和商品对象封装
 * @author Su
 */
@Getter
@Setter
public class FlashPromotionProduct extends PmsProduct{
    @Schema(title = "秒杀关联ID（前端下单时要回传）")
    private Long flashPromotionRelationId;

    @Schema(title = "秒杀活动ID")
    private Long flashPromotionId;

    @Schema(title = "秒杀场次ID")
    private Long flashPromotionSessionId;

    @Schema(title = "秒杀价格")
    private BigDecimal flashPromotionPrice;
    @Schema(title = "用于秒杀到数量")
    private Integer flashPromotionCount;
    @Schema(title = "秒杀限购数量")
    private Integer flashPromotionLimit;
    @Schema(title = "秒杀原始库存数量（用于计算抢购进度）")
    private Integer flashPromotionOriginalCount;
    @Schema(title = "已抢购数量")
    private Integer flashPromotionSold;
}
