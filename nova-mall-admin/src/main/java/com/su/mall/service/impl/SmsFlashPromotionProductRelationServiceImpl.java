package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.SmsFlashPromotionProductRelationDao;
import com.su.mall.dto.SmsFlashPromotionProduct;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.service.SmsFlashPromotionProductRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 限时购商品关联管理Service实现类
 * @author Su
 */
@Service
public class SmsFlashPromotionProductRelationServiceImpl implements SmsFlashPromotionProductRelationService {
    @Autowired
    private SmsFlashPromotionProductRelationMapper relationMapper;
    @Autowired
    private SmsFlashPromotionProductRelationDao relationDao;
    @Override
    public int create(List<SmsFlashPromotionProductRelation> relationList) {
        for (SmsFlashPromotionProductRelation relation : relationList) {
            relationMapper.insert(relation);
        }
        return relationList.size();
    }

    @Override
    public int update(Long id, SmsFlashPromotionProductRelation relation) {
        relation.setId(id);
        return relationMapper.updateById(relation);
    }

    @Override
    public int delete(Long id) {
        // ✅ 改造：deleteByPrimaryKey → deleteById
        return relationMapper.deleteById(id);
    }

    @Override
    public SmsFlashPromotionProductRelation getItem(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return relationMapper.selectById(id);
    }

    @Override
    public Page<SmsFlashPromotionProductRelation> list(Long sessionId, Integer pageSize, Integer pageNum) {
        Page<SmsFlashPromotionProductRelation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsFlashPromotionProductRelation> wrapper = new LambdaQueryWrapper<>();
        return relationMapper.selectPage(page, wrapper);
    }

    @Override
    public long getCount(Long flashPromotionId, Long flashPromotionSessionId) {
        // ✅ 改造：countByExample → selectCount(new LambdaQueryWrapper<>())
        return relationMapper.selectCount(new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, flashPromotionId)
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, flashPromotionSessionId));
    }
}
