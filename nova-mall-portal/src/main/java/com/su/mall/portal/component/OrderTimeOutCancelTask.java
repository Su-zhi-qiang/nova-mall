package com.su.mall.portal.component;

import com.su.mall.portal.service.OmsPortalOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时订单自动取消定时任务
 * <p>每10分钟扫描一次超时未支付的订单，执行取消并释放锁定库存
 * <p>与 {@link CancelOrderReceiver} 的延迟消息机制互补，作为兜底保障
 *
 * @see OmsPortalOrderService#cancelTimeOutOrder()
 */
@Component
@RequiredArgsConstructor
public class OrderTimeOutCancelTask {
    private final Logger LOGGER = LoggerFactory.getLogger(OrderTimeOutCancelTask.class);
    private final OmsPortalOrderService portalOrderService;

    /**
     * 每10分钟执行一次，扫描并取消超时未支付订单
     * <p>cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0/10 * ? * ?")
    private void cancelTimeOutOrder(){
        Integer count = portalOrderService.cancelTimeOutOrder();
        LOGGER.info("取消订单，并根据sku编号释放锁定库存，取消订单数量：{}",count);
    }
}
