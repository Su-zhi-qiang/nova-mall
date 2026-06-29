package com.su.mall.portal.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券领取消息体（MQ消息）
 * <p>由 {@link com.su.mall.portal.component.CouponClaimSender} 发送
 * <p>由 {@link com.su.mall.portal.component.CouponClaimReceiver} 消费处理
 */
@Data
public class CouponClaimMessage implements Serializable {

    /** 会员ID */
    private Long memberId;

    /** 会员昵称（用于生成领取记录） */
    private String memberNickname;

    /** 优惠券ID */
    private Long couponId;
}
