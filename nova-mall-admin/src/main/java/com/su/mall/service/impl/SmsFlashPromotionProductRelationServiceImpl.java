package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.SmsFlashPromotionProductRelationDao;
import com.su.mall.dto.SmsFlashPromotionProduct;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.service.SmsFlashPromotionProductRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 限时购商品关联管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsFlashPromotionProductRelationServiceImpl implements SmsFlashPromotionProductRelationService {
    private final SmsFlashPromotionProductRelationMapper relationMapper;
    private final SmsFlashPromotionProductRelationDao relationDao;

    @Override
    public int create(List<SmsFlashPromotionProductRelation> relationList) {
        for (SmsFlashPromotionProductRelation relation : relationList) {
            if (relation.getOriginalCount() == null && relation.getFlashPromotionCount() != null) {
                relation.setOriginalCount(relation.getFlashPromotionCount());
            }
            relationMapper.insert(relation);
        }
        return relationList.size();
    }

    @Override
    public int update(Long id, SmsFlashPromotionProductRelation relation) {
        relation.setId(id);
        if (relation.getOriginalCount() == null && relation.getFlashPromotionCount() != null) {
            relation.setOriginalCount(relation.getFlashPromotionCount());
        }
        return relationMapper.updateById(relation);
    }

    @Override
    public int delete(Long id) {
        return relationMapper.deleteById(id);
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
        return relationMapper.resetFlashStock(flashPromotionId, flashPromotionSessionId);
    }
}
