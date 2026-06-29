package com.su.mall.portal.component;

import com.su.mall.portal.service.OmsPortalOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 取消订单消息消费者（MQ消费端）
 * <p>监听 mall.order.cancel 队列，接收订单ID后执行取消操作
 * <p>由延迟队列（死信队列）超时后自动转发到此队列
 *
 * @see CancelOrderSender 消息发送端
 * @see com.su.mall.portal.domain.QueueEnum#QUEUE_ORDER_CANCEL
 */
@Component
@RabbitListener(queues = "mall.order.cancel")
@RequiredArgsConstructor
public class CancelOrderReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderReceiver.class);
    private final OmsPortalOrderService portalOrderService;

    /**
     * 处理取消订单消息
     *
     * @param orderId 待取消的订单ID
     */
    @RabbitHandler
    public void handle(Long orderId){
        try {
            portalOrderService.cancelOrder(orderId);
            LOGGER.info("process orderId:{}",orderId);
        } catch (Exception e) {
            LOGGER.info("Order {} has been processed or does not exist, skip cancel: {}", orderId, e.getMessage());
        }
    }
}
