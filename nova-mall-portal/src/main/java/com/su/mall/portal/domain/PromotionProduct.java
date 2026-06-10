package com.su.mall.portal.domain;

import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsProductFullReduction;
import com.su.mall.model.PmsProductLadder;
import com.su.mall.model.PmsSkuStock;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 促销商品信息，包括sku、打折优惠、满减优惠
 * @author Su
 */
@Getter
@Setter
public class PromotionProduct extends PmsProduct {
    private List<PmsSkuStock> skuStockList;
    private List<PmsProductLadder> productLadderList;
    private List<PmsProductFullReduction> productFullReductionList;

    private Long flashPromotionRelationId;
    private Long flashPromotionId;
    private Long flashPromotionSessionId;
    private java.math.BigDecimal flashPromotionPrice;
    private Integer flashPromotionCount;
    private Integer flashPromotionLimit;
}
