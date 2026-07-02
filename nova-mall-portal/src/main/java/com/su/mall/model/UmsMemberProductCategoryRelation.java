package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员商品分类关系
 * 
 */
@Data
public class UmsMemberProductCategoryRelation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long memberId;

    private Long productCategoryId;
}
