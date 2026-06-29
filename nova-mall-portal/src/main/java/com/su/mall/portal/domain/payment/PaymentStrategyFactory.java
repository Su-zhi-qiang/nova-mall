package com.su.mall.portal.domain.payment;

import com.su.mall.portal.config.PaymentTypeConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付策略工厂（工厂模式）
 * <p>启动时从 {@link PaymentTypeConfig} 读取类型映射，将策略Bean缓存到策略池
 * <p>运行时根据payType快速路由到对应的支付策略实例
 *
 * @see PaymentStrategy
 * @see PaymentTypeConfig
 */
@Component
public class PaymentStrategyFactory implements ApplicationContextAware {

    /**
     * 策略池：key=支付类型码，value=对应的支付策略实例
     */
    private static final Map<String, PaymentStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private PaymentTypeConfig paymentTypeConfig;

    /**
     * 初始化策略池，从Spring容器加载所有支付策略Bean
     *
     * @param applicationContext Spring应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, String> types = paymentTypeConfig.getTypes();
        if (types == null || types.isEmpty()) {
            return;
        }
        types.forEach((key, beanName) -> strategyPool.put(key, (PaymentStrategy) applicationContext.getBean(beanName)));
    }

    /**
     * 根据支付类型获取对应的支付策略
     * <p>类型映射示例：1=支付宝, 2=微信
     *
     * @param payType 支付方式码
     * @return 对应的支付策略实例
     * @throws UnsupportedOperationException 未配置的支付类型
     */
    public PaymentStrategy getStrategy(Integer payType) {
        String key = String.valueOf(payType);
        PaymentStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            throw new UnsupportedOperationException("不支持的支付方式: " + payType);
        }
        return strategy;
    }
}
