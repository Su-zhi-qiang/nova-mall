package com.su.mall.portal.domain;

import com.su.mall.model.PmsProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 秒杀商品信息
 * <p>继承商品基础信息，扩展秒杀活动相关字段（秒杀价、库存、限购、已售数量）
 * <p>用于首页秒杀场次商品列表和秒杀详情页展示
 *
 * @see HomeFlashPromotion#productList
 */
@Getter
@Setter
public class FlashPromotionProduct extends PmsProduct {

    @Schema(title = "秒杀关联ID（下单时需回传）")
    private Long flashPromotionRelationId;

    @Schema(title = "秒杀活动ID")
    private Long flashPromotionId;

    @Schema(title = "秒杀场次ID")
    private Long flashPromotionSessionId;

    @Schema(title = "秒杀专属价格")
    private BigDecimal flashPromotionPrice;

    @Schema(title = "秒杀剩余库存数量")
    private Integer flashPromotionCount;

    @Schema(title = "秒杀限购数量（每人限购）")
    private Integer flashPromotionLimit;

    @Schema(title = "秒杀原始库存（用于计算抢购进度百分比）")
    private Integer flashPromotionOriginalCount;

    @Schema(title = "已抢购数量")
    private Integer flashPromotionSold;
}
