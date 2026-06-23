package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.exception.Asserts;
import com.su.mall.mapper.SmsFlashPromotionMapper;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.model.SmsFlashPromotion;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.service.SmsFlashPromotionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 限时购活动管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsFlashPromotionServiceImpl implements SmsFlashPromotionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsFlashPromotionServiceImpl.class);

    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SmsFlashPromotionProductRelationMapper relationMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public int create(SmsFlashPromotion flashPromotion) {
        flashPromotion.setCreateTime(new Date());
        return flashPromotionMapper.insert(flashPromotion);
    }

    @Override
    public int update(Long id, SmsFlashPromotion flashPromotion) {
        SmsFlashPromotion existing = flashPromotionMapper.selectById(id);
        if (existing != null && existing.getStatus() != null && existing.getStatus() == 1) {
            Asserts.fail("不能修改进行中的秒杀活动");
        }
        flashPromotion.setId(id);
        int result = flashPromotionMapper.updateById(flashPromotion);
        clearFlashPromotionCache();
        return result;
    }

    @Override
    @Transactional
    public int delete(Long id) {
        SmsFlashPromotion promotion = flashPromotionMapper.selectById(id);
        if (promotion != null && promotion.getStatus() != null && promotion.getStatus() == 1) {
            Asserts.fail("不能删除进行中的秒杀活动");
        }
        relationMapper.delete(new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, id));
        int result = flashPromotionMapper.deleteById(id);
        clearFlashPromotionCache();
        return result;
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            Asserts.fail("状态值无效，只允许0或1");
        }
        SmsFlashPromotion flashPromotion = new SmsFlashPromotion();
        flashPromotion.setId(id);
        flashPromotion.setStatus(status);
        int result = flashPromotionMapper.updateById(flashPromotion);
        clearFlashPromotionCache();
        return result;
    }

    @Override
    public SmsFlashPromotion getItem(Long id) {
        return flashPromotionMapper.selectById(id);
    }

    @Override
    public Page<SmsFlashPromotion> list(String keyword, Integer pageSize, Integer pageNum) {
        Page<SmsFlashPromotion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsFlashPromotion> wrapper = new LambdaQueryWrapper<>();
        if (!StrUtil.isEmpty(keyword)) {
            wrapper.like(SmsFlashPromotion::getTitle, keyword);
        }
        return flashPromotionMapper.selectPage(page, wrapper);
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
