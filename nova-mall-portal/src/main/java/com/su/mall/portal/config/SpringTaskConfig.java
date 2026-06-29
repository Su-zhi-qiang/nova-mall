package com.su.mall.portal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring定时任务配置
 * <p>启用 @Scheduled 注解支持
 * <p>项目中的定时任务：
 * <ul>
 *   <li>{@link com.su.mall.portal.component.OrderTimeOutCancelTask} - 每10分钟扫描超时订单</li>
 *   <li>{@link com.su.mall.portal.component.FlashPromotionDailyStockTask} - 每日凌晨生成秒杀库存快照</li>
 *   <li>{@link com.su.mall.portal.component.CouponExpirationTask} - 每日凌晨标记过期优惠券</li>
 * </ul>
 */
@Configuration
@EnableScheduling
public class SpringTaskConfig {
}
