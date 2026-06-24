package com.su.mall.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 支付类型配置，从 application.yml 读取映射关系
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentTypeConfig {

    /**
     * 支付方式映射：payType值 -> 策略Bean名称
     * 例如：
     * 1: alipayPaymentStrategy
     * 2: wechatPaymentStrategy
     */
    private Map<String, String> types;
}