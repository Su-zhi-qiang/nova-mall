package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OmsOrderSetting implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(title = "秒杀订单超时关闭时间(分)")
    private Integer flashOrderOvertime;

    @Schema(title = "正常订单超时时间(分)")
    private Integer normalOrderOvertime;

    @Schema(title = "发货后自动确认收货时间（天）")
    private Integer confirmOvertime;

    @Schema(title = "自动完成交易时间，不能申请售后（天）")
    private Integer finishOvertime;

    @Schema(title = "订单完成后自动好评时间（天）")
    private Integer commentOvertime;
}
