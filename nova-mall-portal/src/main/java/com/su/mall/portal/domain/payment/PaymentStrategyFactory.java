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
 * 支付策略工厂，从配置文件读取策略映射
 */
@Component
public class PaymentStrategyFactory implements ApplicationContextAware {

    private static final Map<String, PaymentStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private PaymentTypeConfig paymentTypeConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        paymentTypeConfig.getTypes().forEach((key, beanName) -> {
            strategyPool.put(String.valueOf(key), (PaymentStrategy) applicationContext.getBean(beanName));
        });
    }

    /**
     * 根据支付类型获取支付策略
     * @param payType 支付方式（1=支付宝, 2=微信）
     * @return 对应的策略实例
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