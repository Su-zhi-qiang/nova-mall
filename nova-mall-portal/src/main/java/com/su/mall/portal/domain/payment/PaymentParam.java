package com.su.mall.portal.domain.payment;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 支付参数
 */
@Getter
@Builder
public class PaymentParam {

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 支付金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单标题
     */
    private String subject;

    /**
     * 支付方式：1=支付宝, 2=微信
     */
    private Integer payType;
}