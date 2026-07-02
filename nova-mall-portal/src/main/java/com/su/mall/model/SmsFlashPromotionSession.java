package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 限时购场次
 * 
 */
@Data
public class SmsFlashPromotionSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(title = "编号")
    private Long id;

    @Schema(title = "场次名称")
    private String name;

    @Schema(title = "每日开始时间")
    private Date startTime;

    @Schema(title = "每日结束时间")
    private Date endTime;

    @Schema(title = "启用状态：0->不启用；1->启用")
    private Integer status;

    @Schema(title = "创建时间")
    private Date createTime;
}
