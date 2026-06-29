package com.su.mall.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 促销类型配置
 * <p>从 application.yml 的 promotion.types 读取类型码到策略Bean名称的映射
 * <p>供 {@link com.su.mall.portal.domain.promotion.PromotionStrategyFactory} 初始化策略池
 *
 * @see com.su.mall.portal.domain.promotion.PromotionStrategyFactory
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "promotion")
public class PromotionTypeConfig {

    /**
     * 促销类型映射：promotionType值 → 策略Bean名称
     * <p>配置示例：
     * <pre>
     * promotion:
     *   types:
     *     "-1": flashPromotionStrategy     # 秒杀促销
     *     "0": noPromotionStrategy         # 无促销
     *     "1": singleItemPromotionStrategy # 单品促销
     *     "3": ladderDiscountPromotionStrategy # 阶梯打折
     *     "4": fullReductionPromotionStrategy  # 满减优惠
     * </pre>
     */
    private Map<String, String> types;
}
