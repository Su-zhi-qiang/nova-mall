package com.su.mall.search.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Elasticsearch商品搜索文档
 * <p>与ES索引中的商品文档一一对应，用于全文检索和聚合查询
 * <p>通过 {@link com.su.mall.search.dao.EsProductDao} 从MySQL加载数据后写入ES
 * <p>包含商品基本信息、品牌/分类信息、销售属性、促销状态等搜索相关字段
 */
@Data
@EqualsAndHashCode
public class EsProduct implements Serializable {
    private static final long serialVersionUID = -1L;
    
    private Long id;
    private String productSn;
    private Long brandId;
    private String brandName;
    private Long productCategoryId;
    private String productCategoryName;
    private String pic;
    private String name;
    private String subTitle;
    private String keywords;
    private BigDecimal price;
    private Integer sale;
    private Integer newStatus;
    private Integer recommandStatus;
    private Integer stock;
    private Integer promotionType;
    private Integer sort;
    private List<EsProductAttributeValue> attrValueList;
}
