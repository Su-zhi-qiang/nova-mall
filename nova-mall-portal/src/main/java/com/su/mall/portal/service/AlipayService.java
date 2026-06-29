package com.su.mall.portal.service;


import com.su.mall.portal.domain.AliPayParam;

import java.util.Map;

/**
 * 支付宝支付服务接口
 * <p>封装支付宝API的调用逻辑，提供电脑支付、手机支付、回调处理和交易查询
 *
 * @see com.su.mall.portal.domain.payment.AlipayPaymentStrategy 策略模式实现（已替代此接口）
 * @deprecated 此接口已被 {@link com.su.mall.portal.domain.payment.PaymentStrategy} 策略模式替代
 */
@Deprecated
public interface AlipayService {

    /**
     * 生成电脑端支付页面
     *
     * @param aliPayParam 支付参数
     * @return 支付页面HTML
     */
    String pay(AliPayParam aliPayParam);

    /**
     * 处理支付宝异步回调
     *
     * @param params 回调参数
     * @return "success"或"failure"
     */
    String notify(Map<String, String> params);

    /**
     * 查询支付宝交易状态
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    支付宝交易流水号
     * @return 交易状态字符串
     */
    String query(String outTradeNo, String tradeNo);

    /**
     * 生成手机端支付页面
     *
     * @param aliPayParam 支付参数
     * @return 手机支付页面HTML
     */
    String webPay(AliPayParam aliPayParam);
}
