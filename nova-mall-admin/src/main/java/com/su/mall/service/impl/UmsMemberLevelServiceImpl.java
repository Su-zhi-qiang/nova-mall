package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.UmsMemberLevelMapper;
import com.su.mall.model.UmsMemberLevel;
import com.su.mall.service.UmsMemberLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员等级管理Service实现类
 * 
 */
@Service
@RequiredArgsConstructor
public class UmsMemberLevelServiceImpl implements UmsMemberLevelService {
    private final UmsMemberLevelMapper memberLevelMapper;
    
    @Override
    public List<UmsMemberLevel> list(Integer defaultStatus) {
        LambdaQueryWrapper<UmsMemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (defaultStatus != null) {
            wrapper.eq(UmsMemberLevel::getDefaultStatus, defaultStatus);
        }
        return memberLevelMapper.selectList(wrapper);
    }
}
