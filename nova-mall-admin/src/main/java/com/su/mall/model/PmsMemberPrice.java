package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品会员价格
 * @author Su
 */
@Data
public class PmsMemberPrice implements Serializable {
    private Long id;

    private Long productId;

    private Long memberLevelId;

    @Schema(title = "会员价格")
    private BigDecimal memberPrice;

    private String memberLevelName;

    @Serial
    private static final long serialVersionUID = 1L;
}
