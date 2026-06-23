package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 限时购每日库存快照
 * @author Su
 */
@Data
public class SmsFlashPromotionDailyStock implements Serializable {
    @Schema(title = "编号")
    private Long id;

    @Schema(title = "关联活动商品关系ID")
    private Long relationId;

    @Schema(title = "日期")
    private Date batchDate;

    @Schema(title = "当日剩余库存")
    private Integer stock;

    @Schema(title = "当日已售数量")
    private Integer sold;

    @Schema(title = "创建时间")
    private Date createTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
