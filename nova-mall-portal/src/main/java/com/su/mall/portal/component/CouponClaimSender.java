package com.su.mall.portal.component;

import com.su.mall.portal.domain.CouponClaimMessage;
import com.su.mall.portal.domain.QueueEnum;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * 优惠券领取消息发送者
 */
@Component
@RequiredArgsConstructor
public class CouponClaimSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponClaimSender.class);
    private final AmqpTemplate amqpTemplate;

    public void sendMessage(CouponClaimMessage message) {
        amqpTemplate.convertAndSend(
                QueueEnum.QUEUE_COUPON_CLAIM.getExchange(),
                QueueEnum.QUEUE_COUPON_CLAIM.getRouteKey(),
                message);
        LOGGER.info("优惠券领取消息已发送, couponId: {}, memberId: {}", message.getCouponId(), message.getMemberId());
    }
}
