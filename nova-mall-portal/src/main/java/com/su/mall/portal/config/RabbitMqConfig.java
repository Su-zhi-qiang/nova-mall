package com.su.mall.portal.config;

import com.su.mall.portal.domain.QueueEnum;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消息队列配置
 * <p>定义交换机、队列和绑定关系，配置JSON消息序列化
 * <p>包含两类业务队列：
 * <ul>
 *   <li>订单取消：TTL延迟队列 + 死信消费队列</li>
 *   <li>优惠券领取：直连消费队列</li>
 * </ul>
 *
 * @see QueueEnum 队列枚举定义
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 配置JSON消息转换器，替代默认的Java序列化
     * <p>使消息在RabbitMQ管理界面可读，且支持跨语言消费
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate使用JSON序列化
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 配置监听容器使用JSON反序列化
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // ==================== 订单取消队列 ====================

    /**
     * 订单取消消费队列绑定的直连交换机
     */
    @Bean
    DirectExchange orderDirect() {
        return ExchangeBuilder
                .directExchange(QueueEnum.QUEUE_ORDER_CANCEL.getExchange())
                .durable(true)
                .build();
    }

    /**
     * 订单取消延迟队列绑定的直连交换机
     */
    @Bean
    DirectExchange orderTtlDirect() {
        return ExchangeBuilder
                .directExchange(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getExchange())
                .durable(true)
                .build();
    }

    /**
     * 订单取消消费队列
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(QueueEnum.QUEUE_ORDER_CANCEL.getName());
    }

    /**
     * 订单取消延迟队列（TTL + 死信）
     * <p>消息到期后通过死信交换机转发到消费队列
     */
    @Bean
    public Queue orderTtlQueue() {
        return QueueBuilder
                .durable(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getName())
                .withArgument("x-dead-letter-exchange", QueueEnum.QUEUE_ORDER_CANCEL.getExchange())
                .withArgument("x-dead-letter-routing-key", QueueEnum.QUEUE_ORDER_CANCEL.getRouteKey())
                .build();
    }

    @Bean
    Binding orderBinding(DirectExchange orderDirect, Queue orderQueue) {
        return BindingBuilder
                .bind(orderQueue)
                .to(orderDirect)
                .with(QueueEnum.QUEUE_ORDER_CANCEL.getRouteKey());
    }

    @Bean
    Binding orderTtlBinding(DirectExchange orderTtlDirect, Queue orderTtlQueue) {
        return BindingBuilder
                .bind(orderTtlQueue)
                .to(orderTtlDirect)
                .with(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getRouteKey());
    }

    // ==================== 优惠券领取队列 ====================

    /**
     * 优惠券领取直连交换机
     */
    @Bean
    DirectExchange couponDirect() {
        return ExchangeBuilder
                .directExchange(QueueEnum.QUEUE_COUPON_CLAIM.getExchange())
                .durable(true)
                .build();
    }

    /**
     * 优惠券领取消费队列
     */
    @Bean
    public Queue couponClaimQueue() {
        return new Queue(QueueEnum.QUEUE_COUPON_CLAIM.getName());
    }

    @Bean
    Binding couponBinding(DirectExchange couponDirect, Queue couponClaimQueue) {
        return BindingBuilder
                .bind(couponClaimQueue)
                .to(couponDirect)
                .with(QueueEnum.QUEUE_COUPON_CLAIM.getRouteKey());
    }
}
