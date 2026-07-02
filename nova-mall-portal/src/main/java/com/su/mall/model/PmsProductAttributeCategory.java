package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品属性分类
 * 
 */
@Data
public class PmsProductAttributeCategory implements Serializable {
    private Long id;

    private String name;

    @Schema(title = "属性数量")
    private Integer attributeCount;

    @Schema(title = "参数数量")
    private Integer paramCount;

    @Serial
    private static final long serialVersionUID = 1L;
}
