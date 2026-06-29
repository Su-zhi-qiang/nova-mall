package com.su.mall.portal.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付宝支付请求参数
 * <p>由前端传递到 {@link com.su.mall.portal.controller.AlipayController} 进行支付
 *
 * @see com.su.mall.portal.domain.payment.PaymentParam 统一支付参数
 */
@Data
public class AliPayParam {

    /** 商户订单号（保持唯一性） */
    private String outTradeNo;

    /** 商品标题/订单标题（显示在支付宝收银台） */
    private String subject;

    /** 订单总金额（单位：元，精确到小数点后两位） */
    private BigDecimal totalAmount;
}
