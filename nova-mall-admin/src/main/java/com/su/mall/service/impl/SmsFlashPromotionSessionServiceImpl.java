package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.dto.SmsFlashPromotionSessionDetail;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.mapper.SmsFlashPromotionSessionMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.model.SmsFlashPromotionSession;
import com.su.mall.service.SmsFlashPromotionProductRelationService;
import com.su.mall.service.SmsFlashPromotionSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 限时购场次管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsFlashPromotionSessionServiceImpl implements SmsFlashPromotionSessionService {
    private final SmsFlashPromotionSessionMapper promotionSessionMapper;
    private final SmsFlashPromotionProductRelationService relationService;
    private final SmsFlashPromotionProductRelationMapper relationMapper;

    @Override
    public int create(SmsFlashPromotionSession promotionSession) {
        promotionSession.setCreateTime(new Date());
        return promotionSessionMapper.insert(promotionSession);
    }

    @Override
    public int update(Long id, SmsFlashPromotionSession promotionSession) {
        promotionSession.setId(id);
        return promotionSessionMapper.updateById(promotionSession);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        SmsFlashPromotionSession promotionSession = new SmsFlashPromotionSession();
        promotionSession.setId(id);
        promotionSession.setStatus(status);
        return promotionSessionMapper.updateById(promotionSession);
    }

    @Override
    @Transactional
    public int delete(Long id) {
        // 级联删除该场次下的所有商品关联
        relationMapper.delete(new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, id));
        return promotionSessionMapper.deleteById(id);
    }

    @Override
    public SmsFlashPromotionSession getItem(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return promotionSessionMapper.selectById(id);
    }

    @Override
    public List<SmsFlashPromotionSession> list() {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<>())
        return promotionSessionMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId) {
        List<SmsFlashPromotionSessionDetail> result = new ArrayList<>();
        LambdaQueryWrapper<SmsFlashPromotionSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsFlashPromotionSession::getStatus, 1);
        List<SmsFlashPromotionSession> list = promotionSessionMapper.selectList(wrapper);
        for (SmsFlashPromotionSession promotionSession : list) {
            SmsFlashPromotionSessionDetail detail = new SmsFlashPromotionSessionDetail();
            BeanUtils.copyProperties(promotionSession, detail);
            long count = relationService.getCount(flashPromotionId, promotionSession.getId());
            detail.setProductCount(count);
            result.add(detail);
        }
        return result;
    }

    @Override
    public int resetFlashStock(Long flashPromotionId, Long flashPromotionSessionId) {
        return relationService.resetFlashStock(flashPromotionId, flashPromotionSessionId);
    }
}
