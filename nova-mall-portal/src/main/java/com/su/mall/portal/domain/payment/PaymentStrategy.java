package com.su.mall.portal.domain.payment;

import java.util.Map;

/**
 * 支付策略接口（策略模式核心）
 * <p>定义支付流程的统一契约，所有支付方式（支付宝、微信等）均实现此接口
 * <p>通过 {@link PaymentStrategyFactory} 工厂根据payType获取具体策略实例
 *
 * @see AlipayPaymentStrategy 支付宝支付策略
 * @see PaymentStrategyFactory 支付策略工厂
 */
public interface PaymentStrategy {

    /**
     * 生成电脑端支付页面（PC网页支付）
     *
     * @param param 支付参数，包含商户订单号、金额、标题等
     * @return 支付页面的HTML内容，可直接写入Response返回给前端
     */
    String pay(PaymentParam param);

    /**
     * 生成手机端支付页面（H5/WAP支付）
     *
     * @param param 支付参数
     * @return 手机支付页面的HTML内容
     */
    String webPay(PaymentParam param);

    /**
     * 处理支付平台的异步回调通知
     * <p>需验证签名有效性，确认支付成功后更新订单状态
     *
     * @param params 回调参数（由支付平台以POST方式推送）
     * @return "success"表示处理成功，其他表示失败
     */
    String notify(Map<String, String> params);

    /**
     * 查询交易状态
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    第三方交易流水号（可选）
     * @return 交易状态字符串（如 "TRADE_SUCCESS"）
     */
    String query(String outTradeNo, String tradeNo);
}
