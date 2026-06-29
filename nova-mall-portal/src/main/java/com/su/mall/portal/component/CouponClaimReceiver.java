package com.su.mall.portal.component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.mapper.SmsCouponMapper;
import com.su.mall.model.SmsCoupon;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.portal.domain.CouponClaimMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

/**
 * 优惠券领取消息消费者（MQ消费端）
 * <p>监听 mall.coupon.claim 队列，接收领取消息后在DB层面执行原子扣减库存
 * <p>处理流程：原子SQL扣减库存 → 生成领取记录（含唯一券码）
 *
 * @see CouponClaimSender 消息发送端
 */
@Component
@RabbitListener(queues = "mall.coupon.claim")
@RequiredArgsConstructor
public class CouponClaimReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponClaimReceiver.class);
    private final SmsCouponMapper couponMapper;
    private final SmsCouponHistoryMapper couponHistoryMapper;

    /**
     * 处理优惠券领取消息
     * <p>使用条件更新（WHERE count > 0）保证库存扣减的原子性
     *
     * @param message 领取消息（包含会员ID、优惠券ID）
     */
    @RabbitHandler
    public void handle(CouponClaimMessage message) {
        try {
            Long couponId = message.getCouponId();
            Long memberId = message.getMemberId();

            // 1. 原子SQL扣减优惠券库存（条件：库存>0，防止超卖）
            LambdaUpdateWrapper<SmsCoupon> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SmsCoupon::getId, couponId)
                    .gt(SmsCoupon::getCount, 0)                        // 库存必须大于0
                    .setSql("count = count - 1")                       // 库存减1
                    .setSql("receive_count = COALESCE(receive_count, 0) + 1"); // 已领取数加1
            int updateResult = couponMapper.update(null, updateWrapper);

            // 2. 扣减失败说明库存不足，放弃本次消费
            if (updateResult == 0) {
                LOGGER.warn("优惠券MQ消费: 数据库库存不足, couponId: {}", couponId);
                return;
            }

            // 3. 生成优惠券领取记录
            SmsCouponHistory couponHistory = new SmsCouponHistory();
            couponHistory.setCouponId(couponId);
            couponHistory.setCouponCode(generateCouponCode(memberId)); // 生成唯一券码
            couponHistory.setCreateTime(new Date());
            couponHistory.setMemberId(memberId);
            couponHistory.setMemberNickname(message.getMemberNickname());
            couponHistory.setGetType(1);       // 领取方式：手动领取
            couponHistory.setUseStatus(0);     // 使用状态：未使用

            // 4. 插入领取记录到数据库
            couponHistoryMapper.insert(couponHistory);

            LOGGER.info("优惠券领取成功, couponId: {}, memberId: {}", couponId, memberId);
        } catch (Exception e) {
            LOGGER.error("优惠券MQ消费失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成唯一优惠券码
     * <p>格式：后8位时间戳 + 4位随机数 + 后4位会员ID
     *
     * @param memberId 会员ID
     * @return 16位优惠券码
     */
    private String generateCouponCode(Long memberId) {
        StringBuilder sb = new StringBuilder();

        // 1. 取时间戳后8位（精确到毫秒级别唯一性）
        long timeMillis = System.currentTimeMillis();
        String timeStr = String.valueOf(timeMillis);
        sb.append(timeStr.substring(timeStr.length() - 8));

        // 2. 拼接4位随机数（防并发重复）
        for (int i = 0; i < 4; i++) {
            sb.append(new Random().nextInt(10));
        }

        // 3. 拼接会员ID后4位（标识领取者）
        String memberIdStr = memberId.toString();
        if (memberIdStr.length() <= 4) {
            sb.append(String.format("%04d", memberId));
        } else {
            sb.append(memberIdStr.substring(memberIdStr.length() - 4));
        }

        return sb.toString();
    }
}
