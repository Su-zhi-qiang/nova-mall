package com.su.mall.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 优惠券适用范围类型配置
 * <p>从 application.yml 的 coupon.scopeTypes 读取useType到策略Bean名称的映射
 * <p>供 {@link com.su.mall.portal.domain.coupon.CouponScopeStrategyFactory} 初始化策略池
 *
 * @see com.su.mall.portal.domain.coupon.CouponScopeStrategyFactory
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "coupon")
public class CouponTypeConfig {

    /**
     * 优惠券适用范围映射：useType值 → 策略Bean名称
     * <p>配置示例：
     * <pre>
     * coupon:
     *   scopeTypes:
     *     "0": allProductCouponScopeStrategy   # 全场通用
     *     "1": categoryCouponScopeStrategy     # 指定分类
     *     "2": productCouponScopeStrategy      # 指定商品
     * </pre>
     */
    private Map<String, String> scopeTypes;
}
