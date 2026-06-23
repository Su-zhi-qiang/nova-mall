package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 限时购
 * @author Su
 */
@Data
public class SmsFlashPromotion implements Serializable {
    private Long id;

    @Schema(title = "秒杀时间段名称")
    private String title;

    @Schema(title = "开始日期")
    private Date startDate;

    @Schema(title = "结束日期")
    private Date endDate;

    @Schema(title = "上下线状态")
    private Integer status;

    @Schema(title = "创建时间")
    private Date createTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
