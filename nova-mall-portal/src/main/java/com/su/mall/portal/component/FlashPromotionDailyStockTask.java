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
 * 限时购每日库存快照生成定时任务
 * 每天凌晨执行，为当天启用的活动商品生成库存快照
 * @author Su
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
     * 每天凌晨1点执行，为当天启用的活动商品生成库存快照
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyStock() {
        LOGGER.info("开始生成限时购每日库存快照...");
        Date today = new Date();

        // 1. 查询所有在有效期内的活动
        List<SmsFlashPromotion> activePromotions = flashPromotionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotion>()
                        .eq(SmsFlashPromotion::getStatus, 1)
                        .le(SmsFlashPromotion::getStartDate, today)
                        .ge(SmsFlashPromotion::getEndDate, today));

        if (activePromotions.isEmpty()) {
            LOGGER.info("今日无有效秒杀活动，跳过快照生成");
            return;
        }

        int totalGenerated = 0;

        for (SmsFlashPromotion promotion : activePromotions) {
            // 2. 查询该活动下所有启用的场次
            List<SmsFlashPromotionSession> sessions = sessionMapper.selectList(
                    new LambdaQueryWrapper<SmsFlashPromotionSession>()
                            .eq(SmsFlashPromotionSession::getStatus, 1));

            for (SmsFlashPromotionSession session : sessions) {
                // 3. 查询该场次下所有商品关联
                List<SmsFlashPromotionProductRelation> relations = relationMapper.selectList(
                        new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                                .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, promotion.getId())
                                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, session.getId()));

                for (SmsFlashPromotionProductRelation relation : relations) {
                    // 4. 使用 INSERT IGNORE 防止重复生成（幂等）
                    SmsFlashPromotionDailyStock dailyStock = new SmsFlashPromotionDailyStock();
                    dailyStock.setRelationId(relation.getId());
                    dailyStock.setBatchDate(today);
                    dailyStock.setStock(relation.getOriginalCount() != null ? relation.getOriginalCount() : 0);
                    dailyStock.setSold(0);
                    dailyStock.setCreateTime(new Date());

                    try {
                        dailyStockMapper.insert(dailyStock);
                        totalGenerated++;
                    } catch (Exception e) {
                        // UNIQUE KEY 冲突说明今日已生成，跳过
                        LOGGER.debug("活动{}场次{}商品{}今日快照已存在，跳过",
                                promotion.getId(), session.getId(), relation.getProductId());
                    }
                }
            }
        }

        LOGGER.info("限时购每日库存快照生成完成，共生成{}条记录", totalGenerated);
    }
}
