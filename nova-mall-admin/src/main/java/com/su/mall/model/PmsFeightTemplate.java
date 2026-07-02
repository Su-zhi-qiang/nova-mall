package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 运费模板
 * 
 */
@Data
public class PmsFeightTemplate implements Serializable {
    private Long id;

    private String name;

    @Schema(title = "计费类型:0->按重量；1->按件数")
    private Integer chargeType;

    @Schema(title = "首重kg")
    private BigDecimal firstWeight;

    @Schema(title = "首费（元）")
    private BigDecimal firstFee;

    private BigDecimal continueWeight;

    private BigDecimal continmeFee;

    @Schema(title = "目的地（省、市）")
    private String dest;

    @Serial
    private static final long serialVersionUID = 1L;
}
