package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员商品分类关系
 * @author Su
 */
@Data
public class UmsMemberProductCategoryRelation implements Serializable {
    private Long id;

    private Long memberId;

    private Long productCategoryId;

    @Serial
    private static final long serialVersionUID = 1L;
}
