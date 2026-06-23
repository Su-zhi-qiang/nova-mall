package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品分类属性关系
 * @author Su
 */
@Data
public class PmsProductCategoryAttributeRelation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long productCategoryId;

    private Long productAttributeId;
}
