package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeRecommendSubjectMapper;
import com.su.mall.model.SmsHomeRecommendSubject;
import com.su.mall.service.SmsHomeRecommendSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页专题推荐管理Service实现类
 *
 * @author Su
 */
@Service
public class SmsHomeRecommendSubjectServiceImpl implements SmsHomeRecommendSubjectService {
    @Autowired
    private SmsHomeRecommendSubjectMapper smsHomeRecommendSubjectMapper;
    @Override
    public int create(List<SmsHomeRecommendSubject> recommendSubjectList) {
        for (SmsHomeRecommendSubject recommendSubject : recommendSubjectList) {
            recommendSubject.setRecommendStatus(1);
            recommendSubject.setSort(0);
            smsHomeRecommendSubjectMapper.insert(recommendSubject);
        }
        return recommendSubjectList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        SmsHomeRecommendSubject recommendSubject = new SmsHomeRecommendSubject();
        recommendSubject.setId(id);
        recommendSubject.setSort(sort);
        return smsHomeRecommendSubjectMapper.updateById(recommendSubject);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → delete(new LambdaQueryWrapper<>())
        return smsHomeRecommendSubjectMapper.delete(new LambdaQueryWrapper<SmsHomeRecommendSubject>()
                .in(SmsHomeRecommendSubject::getId, ids));
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        // ✅ 改造：updateByExampleSelective → update(new LambdaQueryWrapper<>().in(...))
        SmsHomeRecommendSubject record = new SmsHomeRecommendSubject();
        record.setRecommendStatus(recommendStatus);
        return smsHomeRecommendSubjectMapper.update(record, new LambdaQueryWrapper<SmsHomeRecommendSubject>()
                .in(SmsHomeRecommendSubject::getId, ids));
    }

    @Override
    public Page<SmsHomeRecommendSubject> list(String subjectName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        Page<SmsHomeRecommendSubject> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<>())
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
