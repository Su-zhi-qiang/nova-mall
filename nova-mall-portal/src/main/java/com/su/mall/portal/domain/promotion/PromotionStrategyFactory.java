package com.su.mall.portal.domain.promotion;

import com.su.mall.portal.config.PromotionTypeConfig;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 促销策略工厂（工厂模式）
 * <p>在应用启动时，根据 {@link PromotionTypeConfig} 中的类型映射配置，
 * <p>从Spring容器获取所有策略Bean实例并缓存到策略池中
 * <p>运行时根据促销类型码快速获取对应的策略实例
 *
 * @see PromotionStrategy
 * @see PromotionTypeConfig
 */
@Component
public class PromotionStrategyFactory implements ApplicationContextAware {

    /**
     * 策略池：key=促销类型码，value=对应的策略实例
     * <p>使用ConcurrentHashMap保证线程安全
     */
    private static final Map<String, PromotionStrategy> strategyPool = new ConcurrentHashMap<>();

    @Autowired
    private PromotionTypeConfig promotionTypeConfig;

    /**
     * 应用上下文初始化回调
     * <p>读取促销类型配置映射，从Spring容器获取对应的策略Bean并缓存到策略池
     *
     * @param applicationContext Spring应用上下文
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        Map<String, String> types = promotionTypeConfig.getTypes();
        if (types == null || types.isEmpty()) {
            return;
        }
        types.forEach((key, beanName) -> strategyPool.put(key, (PromotionStrategy) applicationContext.getBean(beanName)));
    }

    /**
     * 根据促销类型获取对应的策略实例
     * <p>类型映射示例：1=单品促销, 3=打折, 4=满减
     * <p>未匹配到对应类型时，默认返回无优惠策略（key="0"）
     *
     * @param promotionType 促销类型码
     * @return 对应的促销策略实例，永不返回null
     */
    public PromotionStrategy getStrategy(Integer promotionType) {
        String key = String.valueOf(promotionType);
        PromotionStrategy strategy = strategyPool.get(key);
        if (strategy == null) {
            return strategyPool.get("0");
        }
        return strategy;
    }

    /**
     * 获取秒杀促销策略实例
     * <p>秒杀策略不走常规类型映射，使用固定key="flash"
     *
     * @return 秒杀策略实例
     */
    public PromotionStrategy getFlashStrategy() {
        return strategyPool.get("flash");
    }
}
