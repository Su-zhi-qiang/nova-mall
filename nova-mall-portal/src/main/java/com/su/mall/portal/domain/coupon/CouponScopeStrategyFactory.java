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
 * 优惠券适用范围策略工厂，从配置文件读取策略映射
 */
@Component
public class CouponScopeStrategyFactory implements ApplicationContextAware {

    private static final Map<String, CouponScopeStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private CouponTypeConfig couponTypeConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        couponTypeConfig.getScopeTypes().forEach((key, beanName) -> {
            strategyPool.put(key, (CouponScopeStrategy) applicationContext.getBean(beanName));
        });
    }

    /**
     * 根据 useType 获取优惠券适用范围策略
     * @param useType 优惠券使用类型（0=全场通用, 1=指定分类, 2=指定商品）
     * @return 对应的策略实例
     */
    public CouponScopeStrategy getStrategy(Integer useType) {
        String key = String.valueOf(useType);
        CouponScopeStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            return strategyPool.get("0");  // 默认全场通用
        }
        return strategy;
    }
}