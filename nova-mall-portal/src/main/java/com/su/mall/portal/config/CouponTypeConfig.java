package com.su.mall.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 优惠券适用范围类型配置，从 application.yml 读取映射关系
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "coupon")
public class CouponTypeConfig {

    /**
     * 优惠券适用范围映射：useType值 -> 策略Bean名称
     * 例如：
     * 0: allProductCouponScopeStrategy
     * 1: categoryCouponScopeStrategy
     * 2: productCouponScopeStrategy
     */
    private Map<String, String> scopeTypes;
}