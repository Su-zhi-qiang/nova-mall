package com.su.mall.search.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 搜索商品的属性值信息
 * 使用普通POJO，不依赖Spring Data Elasticsearch注解
 * @author Su
 */
@Data
@EqualsAndHashCode
public class EsProductAttributeValue implements Serializable {
    private static final long serialVersionUID = -1L;
    
    private Long id;
    private Long productAttributeId;
    private String value;
    private String name;
    private String type;
}
