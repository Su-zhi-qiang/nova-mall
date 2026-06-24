package com.su.mall.portal.domain.promotion;

import com.su.mall.portal.config.PromotionTypeConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 促销策略工厂，从配置文件读取策略映射
 */
@Component
public class PromotionStrategyFactory implements ApplicationContextAware {

    private static final Map<String, PromotionStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private PromotionTypeConfig promotionTypeConfig;

    /**
     * 从配置文件中读取策略信息存储到map中
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        promotionTypeConfig.getTypes().forEach((key, beanName) -> {
            strategyPool.put(key, (PromotionStrategy) applicationContext.getBean(beanName));
        });
    }

    /**
     * 根据促销类型获取策略实例
     * @param promotionType 促销类型（如 1=单品促销, 3=打折, 4=满减）
     * @return 对应的策略实例
     */
    public PromotionStrategy getStrategy(Integer promotionType) {
        String key = String.valueOf(promotionType);
        PromotionStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            // 默认返回无优惠策略
            return strategyPool.get("0");
        }
        return strategy;
    }

    /**
     * 获取秒杀策略
     */
    public PromotionStrategy getFlashStrategy() {
        return strategyPool.get("flash");
    }
}