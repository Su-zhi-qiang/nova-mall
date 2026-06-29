package com.su.mall.portal.domain.payment;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 支付请求参数封装
 * <p>采用建造者模式构建，供 {@link PaymentStrategy} 各实现类使用
 *
 * @see PaymentStrategy#pay(PaymentParam)
 */
@Getter
@Builder
public class PaymentParam {

    /**
     * 商户订单号（唯一标识）
     */
    private String outTradeNo;

    /**
     * 支付金额（单位：元）
     */
    private BigDecimal totalAmount;

    /**
     * 订单标题（显示在支付页面的商品名称）
     */
    private String subject;

    /**
     * 支付方式码：1=支付宝, 2=微信
     *
     * @see com.su.mall.portal.config.PaymentTypeConfig
     */
    private Integer payType;
}
