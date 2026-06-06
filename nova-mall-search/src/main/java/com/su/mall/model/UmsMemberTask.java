package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class UmsMemberTask implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    @Schema(title = "赠送成长值")
    private Integer growth;

    @Schema(title = "赠送积分")
    private Integer intergration;

    @Schema(title = "任务类型：0->新手任务；1->日常任务")
    private Integer type;
}
