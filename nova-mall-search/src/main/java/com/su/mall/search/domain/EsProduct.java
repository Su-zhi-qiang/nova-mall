package com.su.mall.search.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 搜索商品的信息
 * 使用普通POJO，不依赖Spring Data Elasticsearch注解
 * @author Su
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
