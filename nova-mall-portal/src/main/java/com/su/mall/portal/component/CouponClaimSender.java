package com.su.mall.portal.component;

import com.su.mall.portal.domain.CouponClaimMessage;
import com.su.mall.portal.domain.QueueEnum;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * 优惠券领取消息生产者（MQ发送端）
 * <p>将领取消息发送到 mall.coupon.claim 队列，由 {@link CouponClaimReceiver} 消费
 *
 * @see CouponClaimReceiver 消息消费端
 * @see QueueEnum#QUEUE_COUPON_CLAIM
 */
@Component
@RequiredArgsConstructor
public class CouponClaimSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponClaimSender.class);
    private final AmqpTemplate amqpTemplate;

    /**
     * 发送优惠券领取消息到MQ
     *
     * @param message 领取消息（包含会员ID、优惠券ID、会员昵称）
     */
    public void sendMessage(CouponClaimMessage message) {
        amqpTemplate.convertAndSend(
                QueueEnum.QUEUE_COUPON_CLAIM.getExchange(),
                QueueEnum.QUEUE_COUPON_CLAIM.getRouteKey(),
                message);
        LOGGER.info("优惠券领取消息已发送, couponId: {}, memberId: {}", message.getCouponId(), message.getMemberId());
    }
}
