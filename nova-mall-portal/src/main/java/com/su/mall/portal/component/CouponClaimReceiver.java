package com.su.mall.portal.component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.mapper.SmsCouponMapper;
import com.su.mall.model.SmsCoupon;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.portal.domain.CouponClaimMessage;
import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

/**
 * 优惠券领取消息接收者（MQ消费端执行DB扣减）
 */
@Component
@RabbitListener(queues = "mall.coupon.claim")
@RequiredArgsConstructor
public class CouponClaimReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponClaimReceiver.class);
    private final SmsCouponMapper couponMapper;
    private final SmsCouponHistoryMapper couponHistoryMapper;

    @RabbitHandler
    public void handle(CouponClaimMessage message) {
        try {
            Long couponId = message.getCouponId();
            Long memberId = message.getMemberId();

            // 原子SQL扣减数据库库存
            LambdaUpdateWrapper<SmsCoupon> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SmsCoupon::getId, couponId)
                    .gt(SmsCoupon::getCount, 0)
                    .setSql("count = count - 1")
                    .setSql("receive_count = COALESCE(receive_count, 0) + 1");
            int updateResult = couponMapper.update(null, updateWrapper);
            if (updateResult == 0) {
                LOGGER.warn("优惠券MQ消费: 数据库库存不足, couponId: {}", couponId);
                return;
            }

            // 生成领取记录
            SmsCouponHistory couponHistory = new SmsCouponHistory();
            couponHistory.setCouponId(couponId);
            couponHistory.setCouponCode(generateCouponCode(memberId));
            couponHistory.setCreateTime(new Date());
            couponHistory.setMemberId(memberId);
            couponHistory.setMemberNickname(message.getMemberNickname());
            couponHistory.setGetType(1);
            couponHistory.setUseStatus(0);
            couponHistoryMapper.insert(couponHistory);

            LOGGER.info("优惠券领取成功, couponId: {}, memberId: {}", couponId, memberId);
        } catch (Exception e) {
            LOGGER.error("优惠券MQ消费失败: {}", e.getMessage(), e);
        }
    }

    private String generateCouponCode(Long memberId) {
        StringBuilder sb = new StringBuilder();
        long timeMillis = System.currentTimeMillis();
        String timeStr = String.valueOf(timeMillis);
        sb.append(timeStr.substring(timeStr.length() - 8));
        for (int i = 0; i < 4; i++) {
            sb.append(new Random().nextInt(10));
        }
        String memberIdStr = memberId.toString();
        if (memberIdStr.length() <= 4) {
            sb.append(String.format("%04d", memberId));
        } else {
            sb.append(memberIdStr.substring(memberIdStr.length() - 4));
        }
        return sb.toString();
    }
}
