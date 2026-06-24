package com.su.mall.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 促销类型配置，从 application.yml 读取映射关系
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "promotion")
public class PromotionTypeConfig {

    /**
     * 促销类型映射：promotionType值 -> 策略Bean名称
     * 例如：
     * -1: flashPromotionStrategy
     * 0: noPromotionStrategy
     * 1: singleItemPromotionStrategy
     * 3: ladderDiscountPromotionStrategy
     * 4: fullReductionPromotionStrategy
     */
    private Map<String, String> types;
}