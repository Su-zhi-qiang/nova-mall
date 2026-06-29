package com.su.mall.portal.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝SDK客户端配置
 * <p>根据 {@link AlipayConfig} 中的参数创建AlipayClient Bean
 * <p>AlipayClient是调用支付宝所有API的入口
 *
 * @see AlipayConfig
 * @see com.su.mall.portal.domain.payment.AlipayPaymentStrategy
 */
@Configuration
public class AlipayClientConfig {

    /**
     * 注册支付宝客户端Bean
     *
     * @param config 支付宝配置参数
     * @return AlipayClient实例
     */
    @Bean
    public AlipayClient alipayClient(AlipayConfig config) {
        return new DefaultAlipayClient(
                config.getGatewayUrl(),
                config.getAppId(),
                config.getAppPrivateKey(),
                config.getFormat(),
                config.getCharset(),
                config.getAlipayPublicKey(),
                config.getSignType());
    }
}
