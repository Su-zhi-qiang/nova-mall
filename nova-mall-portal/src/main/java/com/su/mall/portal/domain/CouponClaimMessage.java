package com.su.mall.portal.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券领取MQ消息
 */
@Data
public class CouponClaimMessage implements Serializable {
    private Long memberId;
    private String memberNickname;
    private Long couponId;
}
