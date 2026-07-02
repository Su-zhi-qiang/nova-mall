package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 限时购日志
 * 
 */
@Data
public class SmsFlashPromotionLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer memberId;

    private Long productId;

    private String memberPhone;

    private String productName;

    @Schema(title = "会员订阅时间")
    private Date subscribeTime;

    private Date sendTime;
}
