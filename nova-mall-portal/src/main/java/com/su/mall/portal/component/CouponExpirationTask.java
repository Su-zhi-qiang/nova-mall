package com.su.mall.portal.component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.model.SmsCouponHistory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 优惠券过期定时任务
 * 每天凌晨将已过期的优惠券历史记录的useStatus从0更新为2
 * @author Su
 */
@Component
@RequiredArgsConstructor
public class CouponExpirationTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponExpirationTask.class);

    private final SmsCouponHistoryMapper couponHistoryMapper;

    /**
     * 每天凌晨2点执行，将已过期且未使用的优惠券标记为已过期
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateExpiredCouponHistory() {
        LOGGER.info("开始更新过期优惠券使用状态...");
        Date now = new Date();

        LambdaUpdateWrapper<SmsCouponHistory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SmsCouponHistory::getUseStatus, 0)
                .inSql(SmsCouponHistory::getCouponId,
                        "SELECT id FROM sms_coupon WHERE end_time IS NOT NULL AND end_time < NOW()")
                .set(SmsCouponHistory::getUseStatus, 2);

        int updated = couponHistoryMapper.update(null, updateWrapper);
        LOGGER.info("过期优惠券状态更新完成，共更新{}条记录", updated);
    }
}
