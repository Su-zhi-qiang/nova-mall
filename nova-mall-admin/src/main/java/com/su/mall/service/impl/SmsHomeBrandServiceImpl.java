package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeBrandMapper;
import com.su.mall.model.SmsHomeBrand;
import com.su.mall.service.SmsHomeBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页品牌管理Service实现类
 *
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsHomeBrandServiceImpl implements SmsHomeBrandService {
    private final SmsHomeBrandMapper homeBrandMapper;
    @Override
    public int create(List<SmsHomeBrand> homeBrandList) {
        for (SmsHomeBrand smsHomeBrand : homeBrandList) {
            smsHomeBrand.setRecommendStatus(1);
            smsHomeBrand.setSort(0);
            homeBrandMapper.insert(smsHomeBrand);
        }
        return homeBrandList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        SmsHomeBrand homeBrand = new SmsHomeBrand();
        homeBrand.setId(id);
        homeBrand.setSort(sort);
        return homeBrandMapper.updateById(homeBrand);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → delete(new LambdaQueryWrapper<>())
        return homeBrandMapper.delete(new LambdaQueryWrapper<SmsHomeBrand>()
                .in(SmsHomeBrand::getId, ids));
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        // ✅ 改造：updateByExampleSelective → update(new LambdaQueryWrapper<>().in(...))
        SmsHomeBrand record = new SmsHomeBrand();
        record.setRecommendStatus(recommendStatus);
        return homeBrandMapper.update(record, new LambdaQueryWrapper<SmsHomeBrand>()
                .in(SmsHomeBrand::getId, ids));
    }

    @Override
    public Page<SmsHomeBrand> list(String brandName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        Page<SmsHomeBrand> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<>())
        LambdaQueryWrapper<SmsHomeBrand> wrapper = new LambdaQueryWrapper<>();
        if(!StrUtil.isEmpty(brandName)){
            wrapper.like(SmsHomeBrand::getBrandName, brandName);
        }
        if(recommendStatus!=null){
            wrapper.eq(SmsHomeBrand::getRecommendStatus, recommendStatus);
        }
        wrapper.orderByDesc(SmsHomeBrand::getSort);
        return homeBrandMapper.selectPage(page, wrapper);
    }
}
