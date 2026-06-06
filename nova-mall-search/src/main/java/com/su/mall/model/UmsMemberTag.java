package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UmsMemberTag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    @Schema(title = "自动打标签完成订单数量")
    private Integer finishOrderCount;

    @Schema(title = "自动打标签完成订单金额")
    private BigDecimal finishOrderAmount;
}
