package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.CmsPrefrenceAreaMapper;
import com.su.mall.model.CmsPrefrenceArea;
import com.su.mall.service.CmsPrefrenceAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品优选管理Service实现类
 * @author Su
 */
@Service
public class CmsPrefrenceAreaServiceImpl implements CmsPrefrenceAreaService {
    @Autowired
    private CmsPrefrenceAreaMapper prefrenceAreaMapper;

    @Override
    public List<CmsPrefrenceArea> listAll() {
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        return prefrenceAreaMapper.selectList(new LambdaQueryWrapper<>());
    }
}
