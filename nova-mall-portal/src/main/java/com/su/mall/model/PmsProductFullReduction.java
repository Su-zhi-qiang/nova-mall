package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品满减
 * 
 */
@Data
public class PmsProductFullReduction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long productId;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;
}
