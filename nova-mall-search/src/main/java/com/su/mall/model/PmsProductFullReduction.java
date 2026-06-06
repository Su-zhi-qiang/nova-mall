package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PmsProductFullReduction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long productId;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;
}
