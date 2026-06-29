package com.su.mall.portal.domain;

import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsProductFullReduction;
import com.su.mall.model.PmsProductLadder;
import com.su.mall.model.PmsSkuStock;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 促销商品信息扩展
 * <p>继承商品基础信息，聚合SKU库存、打折规则、满减规则等促销相关数据
 * <p>由 {@link com.su.mall.portal.dao.PortalProductDao#getPromotionProductList} 查询构建
 *
 * @see com.su.mall.portal.domain.promotion.PromotionContext
 */
@Getter
@Setter
public class PromotionProduct extends PmsProduct {

    /** SKU库存列表（包含原价、促销价、锁定库存等） */
    private List<PmsSkuStock> skuStockList;

    /** 阶梯打折规则列表（满N件打X折） */
    private List<PmsProductLadder> productLadderList;

    /** 满减规则列表（满X元减Y元） */
    private List<PmsProductFullReduction> productFullReductionList;

    /** 秒杀关联ID */
    private Long flashPromotionRelationId;

    /** 秒杀活动ID */
    private Long flashPromotionId;

    /** 秒杀场次ID */
    private Long flashPromotionSessionId;

    /** 秒杀专属价格 */
    private java.math.BigDecimal flashPromotionPrice;

    /** 秒杀剩余库存 */
    private Integer flashPromotionCount;

    /** 秒杀限购数量 */
    private Integer flashPromotionLimit;
}
