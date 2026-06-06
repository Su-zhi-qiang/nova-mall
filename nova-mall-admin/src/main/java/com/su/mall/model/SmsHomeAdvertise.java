package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class SmsHomeAdvertise implements Serializable {
    private Long id;

    private String name;

    @Schema(title = "轮播位置：0->PC首页轮播；1->app首页轮播")
    private Integer type;

    private String pic;

    private Date startTime;

    private Date endTime;

    @Schema(title = "上下线状态：0->下线；1->上线")
    private Integer status;

    @Schema(title = "点击数")
    private Integer clickCount;

    @Schema(title = "下单数")
    private Integer orderCount;

    @Schema(title = "链接地址")
    private String url;

    @Schema(title = "备注")
    private String note;

    @Schema(title = "排序")
    private Integer sort;

    @Serial
    private static final long serialVersionUID = 1L;
}
