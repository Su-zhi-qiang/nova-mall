package com.su.mall.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 支付类型配置
 * <p>从 application.yml 的 payment.types 读取支付方式码到策略Bean名称的映射
 * <p>供 {@link com.su.mall.portal.domain.payment.PaymentStrategyFactory} 初始化策略池
 *
 * @see com.su.mall.portal.domain.payment.PaymentStrategyFactory
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentTypeConfig {

    /**
     * 支付方式映射：payType值 → 策略Bean名称
     * <p>配置示例：
     * <pre>
     * payment:
     *   types:
     *     "1": alipayPaymentStrategy   # 支付宝
     *     "2": wechatPaymentStrategy   # 微信支付
     * </pre>
     */
    private Map<String, String> types;
}
