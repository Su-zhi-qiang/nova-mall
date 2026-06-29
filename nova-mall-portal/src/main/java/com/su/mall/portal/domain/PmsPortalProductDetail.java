package com.su.mall.portal.domain;

import com.su.mall.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 前台商品详情页完整数据
 * <p>聚合商品基础信息、品牌、属性、SKU库存、促销规则、可用优惠券及秒杀信息
 * <p>由 {@link com.su.mall.portal.service.PmsPortalProductService#detail(Long)} 构建返回
 */
@Getter
@Setter
public class PmsPortalProductDetail {

    @Schema(title = "商品基础信息")
    private PmsProduct product;

    @Schema(title = "商品品牌")
    private PmsBrand brand;

    @Schema(title = "商品属性定义列表（如：颜色、尺码等）")
    private List<PmsProductAttribute> productAttributeList;

    @Schema(title = "商品属性值列表（手动录入的属性值）")
    private List<PmsProductAttributeValue> productAttributeValueList;

    @Schema(title = "SKU库存列表（各规格组合的价格和库存）")
    private List<PmsSkuStock> skuStockList;

    @Schema(title = "阶梯打折规则列表（满N件打X折）")
    private List<PmsProductLadder> productLadderList;

    @Schema(title = "满减规则列表（满X元减Y元）")
    private List<PmsProductFullReduction> productFullReductionList;

    @Schema(title = "商品可用优惠券列表")
    private List<SmsCoupon> couponList;

    // ========== 秒杀相关字段 ==========

    @Schema(title = "是否参与秒杀活动")
    private Boolean flashPromotion;

    @Schema(title = "秒杀关联ID")
    private Long flashPromotionRelationId;

    @Schema(title = "秒杀价格")
    private java.math.BigDecimal flashPromotionPrice;

    @Schema(title = "秒杀剩余库存")
    private Integer flashPromotionCount;

    @Schema(title = "秒杀已售数量")
    private Integer flashPromotionSold;

    @Schema(title = "秒杀限购数量")
    private Integer flashPromotionLimit;

    @Schema(title = "秒杀场次开始时间")
    private java.util.Date flashSessionStartTime;

    @Schema(title = "秒杀场次结束时间")
    private java.util.Date flashSessionEndTime;
}
