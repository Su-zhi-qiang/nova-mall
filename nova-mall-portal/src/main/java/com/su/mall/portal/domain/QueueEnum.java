package com.su.mall.portal.domain;

import lombok.Getter;

/**
 * RabbitMQ消息队列枚举定义
 * <p>统一管理项目中所有队列的交换机名称、队列名称和路由键
 * <p>包含三类业务队列：订单取消（延迟+消费）、优惠券领取
 *
 * @see com.su.mall.portal.config.RabbitMqConfig 队列绑定配置
 */
@Getter
public enum QueueEnum {

    /**
     * 订单取消消费队列
     * <p>接收来自延迟队列转发的超时订单ID，执行实际取消操作
     */
    QUEUE_ORDER_CANCEL("mall.order.direct", "mall.order.cancel", "mall.order.cancel"),

    /**
     * 订单取消延迟队列（TTL + 死信机制）
     * <p>消息在此队列等待指定时间后，通过死信交换机转发到 {@link #QUEUE_ORDER_CANCEL}
     */
    QUEUE_TTL_ORDER_CANCEL("mall.order.direct.ttl", "mall.order.cancel.ttl", "mall.order.cancel.ttl"),

    /**
     * 优惠券领取队列
     * <p>异步处理优惠券库存扣减和领取记录生成，削峰填谷
     */
    QUEUE_COUPON_CLAIM("mall.coupon.direct", "mall.coupon.claim", "mall.coupon.claim");

    /** 交换机名称 */
    private final String exchange;

    /** 队列名称 */
    private final String name;

    /** 路由键 */
    private final String routeKey;

    QueueEnum(String exchange, String name, String routeKey) {
        this.exchange = exchange;
        this.name = name;
        this.routeKey = routeKey;
    }
}
