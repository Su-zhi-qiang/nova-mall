package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.SmsFlashPromotionProductRelationDao;
import com.su.mall.dto.SmsFlashPromotionProduct;
import com.su.mall.mapper.SmsFlashPromotionDailyStockMapper;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.model.SmsFlashPromotionDailyStock;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.service.SmsFlashPromotionProductRelationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 限时购商品关联管理Service实现类
 * 
 */
@Service
@RequiredArgsConstructor
public class SmsFlashPromotionProductRelationServiceImpl implements SmsFlashPromotionProductRelationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsFlashPromotionProductRelationServiceImpl.class);

    private final SmsFlashPromotionProductRelationMapper relationMapper;
    private final SmsFlashPromotionProductRelationDao relationDao;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public int create(List<SmsFlashPromotionProductRelation> relationList) {
        for (SmsFlashPromotionProductRelation relation : relationList) {
            if (relation.getFlashPromotionCount() != null && relation.getOriginalCount() == null) {
                relation.setOriginalCount(relation.getFlashPromotionCount());
            }
            relationMapper.insert(relation);
            // 同步创建今日快照
            createTodaySnapshot(relation);
        }
        clearFlashPromotionCache();
        return relationList.size();
    }

    @Override
    public int update(Long id, SmsFlashPromotionProductRelation relation) {
        relation.setId(id);
        SmsFlashPromotionProductRelation existing = relationMapper.selectById(id);
        if (existing != null) {
            if (relation.getFlashPromotionCount() != null && !relation.getFlashPromotionCount().equals(existing.getFlashPromotionCount())) {
                relation.setOriginalCount(relation.getFlashPromotionCount());
            } else if (relation.getOriginalCount() == null) {
                relation.setOriginalCount(existing.getOriginalCount());
            }
        }
        int result = relationMapper.updateById(relation);
        // 如果库存变化，同步更新今日快照
        if (existing != null && relation.getFlashPromotionCount() != null
                && !relation.getFlashPromotionCount().equals(existing.getFlashPromotionCount())) {
            updateTodaySnapshot(id, relation.getFlashPromotionCount());
        }
        clearFlashPromotionCache();
        return result;
    }

    @Override
    public int delete(Long id) {
        // 删除关联前先删除今日快照
        dailyStockMapper.delete(new LambdaQueryWrapper<SmsFlashPromotionDailyStock>()
                .eq(SmsFlashPromotionDailyStock::getRelationId, id));
        int result = relationMapper.deleteById(id);
        clearFlashPromotionCache();
        return result;
    }

    @Override
    public SmsFlashPromotionProductRelation getItem(Long id) {
        return relationMapper.selectById(id);
    }

    @Override
    public Page<SmsFlashPromotionProduct> list(Long flashPromotionId, Long flashPromotionSessionId, Integer pageSize, Integer pageNum) {
        Page<SmsFlashPromotionProduct> page = new Page<>(pageNum, pageSize);
        return relationDao.getList(page, flashPromotionId, flashPromotionSessionId);
    }

    @Override
    public long getCount(Long flashPromotionId, Long flashPromotionSessionId) {
        return relationMapper.selectCount(new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, flashPromotionId)
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, flashPromotionSessionId));
    }

    @Override
    public int resetFlashStock(Long flashPromotionId, Long flashPromotionSessionId) {
        // 重置每日快照表的库存
        List<SmsFlashPromotionProductRelation> relations = relationMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                        .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, flashPromotionId)
                        .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, flashPromotionSessionId));
        for (SmsFlashPromotionProductRelation relation : relations) {
            SmsFlashPromotionDailyStock stock = dailyStockMapper.selectOne(
                    new LambdaQueryWrapper<SmsFlashPromotionDailyStock>()
                            .eq(SmsFlashPromotionDailyStock::getRelationId, relation.getId())
                            .eq(SmsFlashPromotionDailyStock::getBatchDate, new java.util.Date()));
            if (stock != null) {
                stock.setStock(relation.getOriginalCount() != null ? relation.getOriginalCount() : 0);
                stock.setSold(0);
                dailyStockMapper.updateById(stock);
            }
        }
        return relations.size();
    }

    /**
     * 创建今日快照（如果不存在则新建，已存在则跳过）
     */
    private void createTodaySnapshot(SmsFlashPromotionProductRelation relation) {
        try {
            SmsFlashPromotionDailyStock existing = dailyStockMapper.selectOne(
                    new LambdaQueryWrapper<SmsFlashPromotionDailyStock>()
                            .eq(SmsFlashPromotionDailyStock::getRelationId, relation.getId())
                            .eq(SmsFlashPromotionDailyStock::getBatchDate, new Date()));
            if (existing == null) {
                SmsFlashPromotionDailyStock stock = new SmsFlashPromotionDailyStock();
                stock.setRelationId(relation.getId());
                stock.setBatchDate(new Date());
                stock.setStock(relation.getOriginalCount() != null ? relation.getOriginalCount() : 0);
                stock.setSold(0);
                stock.setCreateTime(new Date());
                dailyStockMapper.insert(stock);
                LOGGER.info("为新关联创建今日快照: relationId={}, stock={}", relation.getId(), stock.getStock());
            }
        } catch (Exception e) {
            LOGGER.warn("创建今日快照失败: {}", e.getMessage());
        }
    }

    /**
     * 更新今日快照库存（当管理员修改秒杀数量时同步更新）
     */
    private void updateTodaySnapshot(Long relationId, Integer newCount) {
        try {
            SmsFlashPromotionDailyStock stock = dailyStockMapper.selectOne(
                    new LambdaQueryWrapper<SmsFlashPromotionDailyStock>()
                            .eq(SmsFlashPromotionDailyStock::getRelationId, relationId)
                            .eq(SmsFlashPromotionDailyStock::getBatchDate, new Date()));
            if (stock != null) {
                // 根据新库存和已售数量重新计算剩余库存
                int newStock = newCount - (stock.getSold() != null ? stock.getSold() : 0);
                if (newStock < 0) newStock = 0;
                stock.setStock(newStock);
                dailyStockMapper.updateById(stock);
                LOGGER.info("更新今日快照: relationId={}, newStock={}", relationId, newStock);
            } else {
                // 快照不存在则创建
                SmsFlashPromotionProductRelation relation = relationMapper.selectById(relationId);
                if (relation != null) {
                    createTodaySnapshot(relation);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("更新今日快照失败: {}", e.getMessage());
        }
    }

    /**
     * 清除秒杀活动相关的Redis缓存
     */
    private void clearFlashPromotionCache() {
        try {
            Set<String> keys = redisTemplate.keys("home:flashPromotion:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                LOGGER.info("已清除秒杀缓存，共{}个key", keys.size());
            }
        } catch (Exception e) {
            LOGGER.warn("清除秒杀缓存失败: {}", e.getMessage());
        }
    }
}
