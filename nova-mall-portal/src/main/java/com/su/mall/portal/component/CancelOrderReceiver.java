package com.su.mall.portal.component;

import com.su.mall.portal.service.OmsPortalOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 *  取消订单消息的接收者
 * @author Su
 */
@Component
@RabbitListener(queues = "mall.order.cancel")
@RequiredArgsConstructor
public class CancelOrderReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderReceiver.class);
    private final OmsPortalOrderService portalOrderService;
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