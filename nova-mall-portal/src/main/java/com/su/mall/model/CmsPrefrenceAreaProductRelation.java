package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CmsPrefrenceAreaProductRelation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long prefrenceAreaId;

    private Long productId;
}
