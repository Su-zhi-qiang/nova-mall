package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专题和产品关系
 * 
 */
@Data
public class CmsSubjectProductRelation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long subjectId;

    private Long productId;
}
