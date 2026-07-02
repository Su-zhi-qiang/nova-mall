package com.su.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.dto.SmsFlashPromotionSessionDetail;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.mapper.SmsFlashPromotionSessionMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.model.SmsFlashPromotionSession;
import com.su.mall.service.SmsFlashPromotionProductRelationService;
import com.su.mall.service.SmsFlashPromotionSessionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 限时购场次管理Service实现类
 * 
 */
@Service
@RequiredArgsConstructor
public class SmsFlashPromotionSessionServiceImpl implements SmsFlashPromotionSessionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsFlashPromotionSessionServiceImpl.class);

    private final SmsFlashPromotionSessionMapper promotionSessionMapper;
    private final SmsFlashPromotionProductRelationService relationService;
    private final SmsFlashPromotionProductRelationMapper relationMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public int create(SmsFlashPromotionSession promotionSession) {
        promotionSession.setCreateTime(new Date());
        return promotionSessionMapper.insert(promotionSession);
    }

    @Override
    public int update(Long id, SmsFlashPromotionSession promotionSession) {
        promotionSession.setId(id);
        int result = promotionSessionMapper.updateById(promotionSession);
        clearFlashPromotionCache();
        return result;
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsFlashPromotionSession promotionSession = new SmsFlashPromotionSession();
        promotionSession.setId(id);
        promotionSession.setStatus(status);
        int result = promotionSessionMapper.updateById(promotionSession);
        clearFlashPromotionCache();
        return result;
    }

    @Override
    @Transactional
    public int delete(Long id) {
        relationMapper.delete(new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, id));
        int result = promotionSessionMapper.deleteById(id);
        clearFlashPromotionCache();
        return result;
    }

    @Override
    public SmsFlashPromotionSession getItem(Long id) {
        return promotionSessionMapper.selectById(id);
    }

    @Override
    public List<SmsFlashPromotionSession> list() {
        return promotionSessionMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId) {
        List<SmsFlashPromotionSessionDetail> result = new ArrayList<>();
        LambdaQueryWrapper<SmsFlashPromotionSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsFlashPromotionSession::getStatus, 1);
        List<SmsFlashPromotionSession> list = promotionSessionMapper.selectList(wrapper);

        // 批量查询所有场次的商品数量（解决N+1问题）
        Map<Long, Long> countMap = new HashMap<>();
        if (flashPromotionId != null && CollUtil.isNotEmpty(list)) {
            List<Long> sessionIds = list.stream().map(SmsFlashPromotionSession::getId).collect(Collectors.toList());
            List<SmsFlashPromotionProductRelation> relations = relationMapper.selectList(
                    new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                            .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, flashPromotionId)
                            .in(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, sessionIds));
            countMap = relations.stream()
                    .collect(Collectors.groupingBy(
                            SmsFlashPromotionProductRelation::getFlashPromotionSessionId,
                            Collectors.counting()));
        }

        for (SmsFlashPromotionSession promotionSession : list) {
            SmsFlashPromotionSessionDetail detail = new SmsFlashPromotionSessionDetail();
            BeanUtils.copyProperties(promotionSession, detail);
            detail.setProductCount(countMap.getOrDefault(promotionSession.getId(), 0L));
            result.add(detail);
        }
        return result;
    }

    @Override
    public int resetFlashStock(Long flashPromotionId, Long flashPromotionSessionId) {
        return relationService.resetFlashStock(flashPromotionId, flashPromotionSessionId);
    }

    private void clearFlashPromotionCache() {
        try {
            Set<String> keys = redisTemplate.keys("home:flashPromotion:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            LOGGER.warn("清除秒杀缓存失败: {}", e.getMessage());
        }
    }
}
