package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 退货原因
 * @author Su
 */
@Data
public class OmsOrderReturnReason implements Serializable {
    private Long id;

    @Schema(title = "退货类型")
    private String name;

    private Integer sort;

    @Schema(title = "状态：0->不启用；1->启用")
    private Integer status;

    @Schema(title = "添加时间")
    private Date createTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
