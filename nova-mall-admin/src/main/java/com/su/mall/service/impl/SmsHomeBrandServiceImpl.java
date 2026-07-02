package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeBrandMapper;
import com.su.mall.model.SmsHomeBrand;
import com.su.mall.service.SmsHomeBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页品牌管理Service实现类
 *
 * 
 */
@Service
@RequiredArgsConstructor
public class SmsHomeBrandServiceImpl implements SmsHomeBrandService {
    private final SmsHomeBrandMapper homeBrandMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private void clearHomeContentCache() {
        try {
            redisTemplate.delete("home:content");
        } catch (Exception e) {
            // 缓存清除失败不影响业务
        }
    }
    @Override
    public int create(List<SmsHomeBrand> homeBrandList) {
        for (SmsHomeBrand smsHomeBrand : homeBrandList) {
            smsHomeBrand.setRecommendStatus(1);
            smsHomeBrand.setSort(0);
            homeBrandMapper.insert(smsHomeBrand);
        }
        clearHomeContentCache();
        return homeBrandList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeBrand homeBrand = new SmsHomeBrand();
        homeBrand.setId(id);
        homeBrand.setSort(sort);
        int result = homeBrandMapper.updateById(homeBrand);
        clearHomeContentCache();
        return result;
    }

    @Override
    public int delete(List<Long> ids) {
        int result = homeBrandMapper.delete(new LambdaQueryWrapper<SmsHomeBrand>()
                .in(SmsHomeBrand::getId, ids));
        clearHomeContentCache();
        return result;
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        SmsHomeBrand record = new SmsHomeBrand();
        record.setRecommendStatus(recommendStatus);
        int result = homeBrandMapper.update(record, new LambdaQueryWrapper<SmsHomeBrand>()
                .in(SmsHomeBrand::getId, ids));
        clearHomeContentCache();
        return result;
    }

    @Override
    public Page<SmsHomeBrand> list(String brandName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        Page<SmsHomeBrand> page = new Page<>(pageNum, pageSize);
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
