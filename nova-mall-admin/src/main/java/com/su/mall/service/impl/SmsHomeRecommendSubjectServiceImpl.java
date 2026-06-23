package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeRecommendSubjectMapper;
import com.su.mall.model.SmsHomeRecommendSubject;
import com.su.mall.service.SmsHomeRecommendSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页专题推荐管理Service实现类
 *
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsHomeRecommendSubjectServiceImpl implements SmsHomeRecommendSubjectService {
    private final SmsHomeRecommendSubjectMapper smsHomeRecommendSubjectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private void clearHomeContentCache() {
        try {
            redisTemplate.delete("home:content");
        } catch (Exception e) {
            // 缓存清除失败不影响业务
        }
    }
    @Override
    public int create(List<SmsHomeRecommendSubject> recommendSubjectList) {
        for (SmsHomeRecommendSubject recommendSubject : recommendSubjectList) {
            recommendSubject.setRecommendStatus(1);
            recommendSubject.setSort(0);
            smsHomeRecommendSubjectMapper.insert(recommendSubject);
        }
        clearHomeContentCache();
        return recommendSubjectList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeRecommendSubject recommendSubject = new SmsHomeRecommendSubject();
        recommendSubject.setId(id);
        recommendSubject.setSort(sort);
        int result = smsHomeRecommendSubjectMapper.updateById(recommendSubject);
        clearHomeContentCache();
        return result;
    }

    @Override
    public int delete(List<Long> ids) {
        int result = smsHomeRecommendSubjectMapper.delete(new LambdaQueryWrapper<SmsHomeRecommendSubject>()
                .in(SmsHomeRecommendSubject::getId, ids));
        clearHomeContentCache();
        return result;
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        SmsHomeRecommendSubject record = new SmsHomeRecommendSubject();
        record.setRecommendStatus(recommendStatus);
        int result = smsHomeRecommendSubjectMapper.update(record, new LambdaQueryWrapper<SmsHomeRecommendSubject>()
                .in(SmsHomeRecommendSubject::getId, ids));
        clearHomeContentCache();
        return result;
    }

    @Override
    public Page<SmsHomeRecommendSubject> list(String subjectName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        Page<SmsHomeRecommendSubject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeRecommendSubject> wrapper = new LambdaQueryWrapper<>();
        if(!StrUtil.isEmpty(subjectName)){
            wrapper.like(SmsHomeRecommendSubject::getSubjectName, subjectName);
        }
        if(recommendStatus!=null){
            wrapper.eq(SmsHomeRecommendSubject::getRecommendStatus, recommendStatus);
        }
        wrapper.orderByDesc(SmsHomeRecommendSubject::getSort);
        return smsHomeRecommendSubjectMapper.selectPage(page, wrapper);
    }
}
