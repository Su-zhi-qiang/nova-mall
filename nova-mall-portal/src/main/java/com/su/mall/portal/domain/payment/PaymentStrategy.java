package com.su.mall.portal.domain.payment;

import java.util.Map;

/**
 * 支付策略接口
 */
public interface PaymentStrategy {

    /**
     * 生成电脑端支付页面
     */
    String pay(PaymentParam param);

    /**
     * 生成手机端支付页面
     */
    String webPay(PaymentParam param);

    /**
     * 异步回调处理
     */
    String notify(Map<String, String> params);

    /**
     * 查询交易状态
     */
    String query(String outTradeNo, String tradeNo);
}