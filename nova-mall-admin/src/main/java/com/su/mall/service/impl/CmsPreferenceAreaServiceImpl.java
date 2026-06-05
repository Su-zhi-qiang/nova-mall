package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.CmsPrefrenceAreaMapper;
import com.su.mall.model.CmsPrefrenceArea;
import com.su.mall.service.CmsPreferenceAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品优选管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class CmsPreferenceAreaServiceImpl implements CmsPreferenceAreaService {
    private final CmsPrefrenceAreaMapper preferenceAreaMapper;

    @Override
    public List<CmsPrefrenceArea> listAll() {
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        return preferenceAreaMapper.selectList(new LambdaQueryWrapper<>());
    }
}
