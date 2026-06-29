package com.su.mall.portal.component;

import com.su.mall.portal.domain.QueueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 取消订单消息生产者（MQ发送端）
 * <p>向延迟队列（TTL队列）发送取消消息，消息到期后通过死信机制转发到消费队列
 * <p>实现订单超时未支付自动取消的延迟触发
 *
 * @see CancelOrderReceiver 消息消费端
 * @see QueueEnum#QUEUE_TTL_ORDER_CANCEL 延迟队列
 */
@Component
@RequiredArgsConstructor
public class CancelOrderSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderSender.class);
    private final AmqpTemplate amqpTemplate;

    /**
     * 发送延迟取消订单消息
     *
     * @param orderId    待取消的订单ID
     * @param delayTimes 延迟时间（毫秒），消息在此时间后被消费
     */
    public void sendMessage(Long orderId,final long delayTimes){
        amqpTemplate.convertAndSend(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getExchange(), QueueEnum.QUEUE_TTL_ORDER_CANCEL.getRouteKey(), orderId, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration(String.valueOf(delayTimes));
                return message;
            }
        });
        LOGGER.info("send orderId:{}",orderId);
    }
}
