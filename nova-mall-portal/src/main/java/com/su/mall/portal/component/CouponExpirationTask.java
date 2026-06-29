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
 * 优惠券过期状态同步定时任务
 * <p>每天凌晨2点扫描已过期但未使用的优惠券记录，将useStatus从0(未使用)更新为2(已过期)
 * <p>通过子查询匹配sms_coupon表的end_time判断是否过期
 *
 * @see SmsCouponHistory#useStatus
 */
@Component
@RequiredArgsConstructor
public class CouponExpirationTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouponExpirationTask.class);

    private final SmsCouponHistoryMapper couponHistoryMapper;

    /**
     * 每天凌晨2点执行，批量标记过期优惠券
     * <p>cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateExpiredCouponHistory() {
        LOGGER.info("开始更新过期优惠券使用状态...");
        Date now = new Date();

        // 1. 构建批量更新条件
        LambdaUpdateWrapper<SmsCouponHistory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(SmsCouponHistory::getUseStatus, 0)  // 当前状态为"未使用"
                .inSql(SmsCouponHistory::getCouponId,
                        // 2. 子查询：查找已过期的优惠券ID（end_time < 当前时间）
                        "SELECT id FROM sms_coupon WHERE end_time IS NOT NULL AND end_time < NOW()")
                .set(SmsCouponHistory::getUseStatus, 2); // 3. 更新状态为"已过期"

        // 4. 执行批量更新
        int updated = couponHistoryMapper.update(null, updateWrapper);
        LOGGER.info("过期优惠券状态更新完成，共更新{}条记录", updated);
    }
}
