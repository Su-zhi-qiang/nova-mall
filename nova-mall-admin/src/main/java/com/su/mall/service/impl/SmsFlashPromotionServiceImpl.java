package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsFlashPromotionMapper;
import com.su.mall.model.SmsFlashPromotion;
import com.su.mall.service.SmsFlashPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 限时购活动管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsFlashPromotionServiceImpl implements SmsFlashPromotionService {
    private final SmsFlashPromotionMapper flashPromotionMapper;

    @Override
    public int create(SmsFlashPromotion flashPromotion) {
        flashPromotion.setCreateTime(new Date());
        return flashPromotionMapper.insert(flashPromotion);
    }

    @Override
    public int update(Long id, SmsFlashPromotion flashPromotion) {
        flashPromotion.setId(id);
        return flashPromotionMapper.updateById(flashPromotion);
    }

    @Override
    public int delete(Long id) {
        // ✅ 改造：deleteByPrimaryKey → deleteById
        return flashPromotionMapper.deleteById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        SmsFlashPromotion flashPromotion = new SmsFlashPromotion();
        flashPromotion.setId(id);
        flashPromotion.setStatus(status);
        return flashPromotionMapper.updateById(flashPromotion);
    }

    @Override
    public SmsFlashPromotion getItem(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return flashPromotionMapper.selectById(id);
    }

    @Override
    public Page<SmsFlashPromotion> list(String keyword, Integer pageSize, Integer pageNum) {
        Page<SmsFlashPromotion> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<>())
        LambdaQueryWrapper<SmsFlashPromotion> wrapper = new LambdaQueryWrapper<>();
        if (!StrUtil.isEmpty(keyword)) {
            wrapper.like(SmsFlashPromotion::getTitle, keyword);
        }
        return flashPromotionMapper.selectPage(page, wrapper);
    }
}
