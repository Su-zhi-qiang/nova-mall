package com.su.mall.portal.domain;

import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsProductAttribute;
import com.su.mall.model.PmsSkuStock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 购物车商品详情（含规格和SKU信息）
 * <p>继承商品基础信息，扩展商品属性列表和SKU库存列表
 * <p>用于购物车页面展示商品规格供用户重选
 *
 * @see com.su.mall.portal.service.OmsCartItemService#getCartProduct
 */
@Getter
@Setter
public class CartProduct extends PmsProduct {

    @Schema(title = "商品属性列表（如：颜色、尺码等可选规格）")
    private List<PmsProductAttribute> productAttributeList;

    @Schema(title = "商品SKU库存列表（包含各规格组合的价格和库存）")
    private List<PmsSkuStock> skuStockList;
}
