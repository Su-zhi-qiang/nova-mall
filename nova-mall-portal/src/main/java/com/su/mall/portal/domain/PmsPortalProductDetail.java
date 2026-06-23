package com.su.mall.portal.domain;

import com.su.mall.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 前台商品详情
 * @author Su
 */
@Getter
@Setter
public class PmsPortalProductDetail{
    @Schema(title = "商品信息")
    private PmsProduct product;
    @Schema(title = "商品品牌")
    private PmsBrand brand;
    @Schema(title = "商品属性与参数")
    private List<PmsProductAttribute> productAttributeList;
    @Schema(title = "手动录入的商品属性与参数值")
    private List<PmsProductAttributeValue> productAttributeValueList;
    @Schema(title = "商品的sku库存信息")
    private List<PmsSkuStock> skuStockList;
    @Schema(title = "商品阶梯价格设置")
    private List<PmsProductLadder> productLadderList;
    @Schema(title = "商品满减价格设置")
    private List<PmsProductFullReduction> productFullReductionList;
    @Schema(title = "商品可用优惠券")
    private List<SmsCoupon> couponList;

    // ========== 秒杀相关字段 ==========
    @Schema(title = "是否在秒杀活动中")
    private Boolean flashPromotion;

    @Schema(title = "秒杀关联ID")
    private Long flashPromotionRelationId;

    @Schema(title = "秒杀价格")
    private java.math.BigDecimal flashPromotionPrice;

    @Schema(title = "秒杀剩余库存")
    private Integer flashPromotionCount;

    @Schema(title = "秒杀已售数量")
    private Integer flashPromotionSold;

    @Schema(title = "限购数量")
    private Integer flashPromotionLimit;

    @Schema(title = "秒杀场次开始时间")
    private java.util.Date flashSessionStartTime;

    @Schema(title = "秒杀场次结束时间")
    private java.util.Date flashSessionEndTime;
}
