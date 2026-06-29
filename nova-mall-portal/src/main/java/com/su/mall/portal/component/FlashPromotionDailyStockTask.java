package com.su.mall.portal.component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.SmsFlashPromotionDailyStockMapper;
import com.su.mall.mapper.SmsFlashPromotionMapper;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.mapper.SmsFlashPromotionSessionMapper;
import com.su.mall.model.SmsFlashPromotion;
import com.su.mall.model.SmsFlashPromotionDailyStock;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.model.SmsFlashPromotionSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 秒杀每日库存快照生成定时任务
 * <p>每天凌晨1点扫描所有有效活动，为当天启用的活动商品生成库存快照记录
 * <p>快照记录用于计算每个秒杀商品的每日剩余库存，支持多场次独立限量
 * <p>使用INSERT IGNORE保证幂等性，重复执行不会产生重复数据
 *
 * @see SmsFlashPromotionDailyStock 每日库存快照表
 */
@Component
@RequiredArgsConstructor
public class FlashPromotionDailyStockTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlashPromotionDailyStockTask.class);

    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SmsFlashPromotionSessionMapper sessionMapper;
    private final SmsFlashPromotionProductRelationMapper relationMapper;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;

    /**
     * 每天凌晨1点执行，为有效秒杀活动生成当日库存快照
     * <p>cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyStock() {
        LOGGER.info("开始生成限时购每日库存快照...");
        Date today = new Date();

        // 1. 查询所有在有效期内且已启用的秒杀活动
        List<SmsFlashPromotion> activePromotions = flashPromotionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotion>()
                        .eq(SmsFlashPromotion::getStatus, 1)        // 状态：已启用
                        .le(SmsFlashPromotion::getStartDate, today)  // 开始日期 <= 今天
                        .ge(SmsFlashPromotion::getEndDate, today));  // 结束日期 >= 今天

        // 2. 今日无有效活动，直接返回
        if (activePromotions.isEmpty()) {
            LOGGER.info("今日无有效秒杀活动，跳过快照生成");
            return;
        }

        int totalGenerated = 0;

        // 3. 遍历每个有效活动
        for (SmsFlashPromotion promotion : activePromotions) {
            // 4. 查询该活动下所有已启用的场次
            List<SmsFlashPromotionSession> sessions = sessionMapper.selectList(
                    new LambdaQueryWrapper<SmsFlashPromotionSession>()
                            .eq(SmsFlashPromotionSession::getStatus, 1));

            // 5. 遍历每个场次
            for (SmsFlashPromotionSession session : sessions) {
                // 6. 查询该场次下所有商品关联关系
                List<SmsFlashPromotionProductRelation> relations = relationMapper.selectList(
                        new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                                .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, promotion.getId())
                                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, session.getId()));

                // 7. 为每个商品生成当日库存快照
                for (SmsFlashPromotionProductRelation relation : relations) {
                    SmsFlashPromotionDailyStock dailyStock = new SmsFlashPromotionDailyStock();
                    dailyStock.setRelationId(relation.getId());
                    dailyStock.setBatchDate(today);
                    dailyStock.setStock(relation.getOriginalCount() != null ? relation.getOriginalCount() : 0);
                    dailyStock.setSold(0); // 初始已售为0
                    dailyStock.setCreateTime(new Date());

                    try {
                        // 8. 插入快照记录（UNIQUE KEY防止重复生成）
                        dailyStockMapper.insert(dailyStock);
                        totalGenerated++;
                    } catch (Exception e) {
                        // 9. UNIQUE KEY冲突说明今日已生成，跳过（幂等性保证）
                        LOGGER.debug("活动{}场次{}商品{}今日快照已存在，跳过",
                                promotion.getId(), session.getId(), relation.getProductId());
                    }
                }
            }
        }

        LOGGER.info("限时购每日库存快照生成完成，共生成{}条记录", totalGenerated);
    }
}
