package com.su.mall.portal.domain.coupon;

import com.su.mall.portal.config.CouponTypeConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优惠券适用范围策略工厂（工厂模式）
 * <p>启动时从 {@link CouponTypeConfig} 读取useType到Bean名称的映射
 * <p>运行时根据优惠券的useType路由到对应的适用范围策略
 *
 * @see CouponScopeStrategy
 * @see CouponTypeConfig
 */
@Component
public class CouponScopeStrategyFactory implements ApplicationContextAware {

    /**
     * 策略池：key=优惠券使用类型码，value=对应的适用范围策略实例
     */
    private static final Map<String, CouponScopeStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private CouponTypeConfig couponTypeConfig;

    /**
     * 初始化策略池
     *
     * @param applicationContext Spring应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, String> scopeTypes = couponTypeConfig.getScopeTypes();
        if (scopeTypes == null || scopeTypes.isEmpty()) {
            return;
        }
        scopeTypes.forEach((key, beanName) -> strategyPool.put(key, (CouponScopeStrategy) applicationContext.getBean(beanName)));
    }

    /**
     * 根据优惠券使用类型获取适用范围策略
     * <p>类型映射：0=全场通用, 1=指定分类, 2=指定商品
     * <p>未匹配时默认返回全场通用策略（key="0"）
     *
     * @param useType 优惠券使用类型码
     * @return 对应的适用范围策略实例，永不返回null
     */
    public CouponScopeStrategy getStrategy(Integer useType) {
        String key = String.valueOf(useType);
        CouponScopeStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            return strategyPool.get("0");
        }
        return strategy;
    }
}
