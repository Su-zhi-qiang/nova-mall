package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeNewProductMapper;
import com.su.mall.model.SmsHomeNewProduct;
import com.su.mall.service.SmsHomeNewProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页新品推荐管理Service实现类
 *
 * 
 */
@Service
@RequiredArgsConstructor
public class SmsHomeNewProductServiceImpl implements SmsHomeNewProductService {
    private final SmsHomeNewProductMapper homeNewProductMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private void clearHomeContentCache() {
        try {
            redisTemplate.delete("home:content");
        } catch (Exception e) {
            // 缓存清除失败不影响业务
        }
    }
    @Override
    public int create(List<SmsHomeNewProduct> homeNewProductList) {
        for (SmsHomeNewProduct SmsHomeNewProduct : homeNewProductList) {
            SmsHomeNewProduct.setRecommendStatus(1);
            SmsHomeNewProduct.setSort(0);
            homeNewProductMapper.insert(SmsHomeNewProduct);
        }
        clearHomeContentCache();
        return homeNewProductList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeNewProduct homeNewProduct = new SmsHomeNewProduct();
        homeNewProduct.setId(id);
        homeNewProduct.setSort(sort);
        int result = homeNewProductMapper.updateById(homeNewProduct);
        clearHomeContentCache();
        return result;
    }

    @Override
    public int delete(List<Long> ids) {
        int result = homeNewProductMapper.delete(new LambdaQueryWrapper<SmsHomeNewProduct>()
                .in(SmsHomeNewProduct::getId, ids));
        clearHomeContentCache();
        return result;
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        SmsHomeNewProduct record = new SmsHomeNewProduct();
        record.setRecommendStatus(recommendStatus);
        int result = homeNewProductMapper.update(record, new LambdaQueryWrapper<SmsHomeNewProduct>()
                .in(SmsHomeNewProduct::getId, ids));
        clearHomeContentCache();
        return result;
    }

    @Override
    public Page<SmsHomeNewProduct> list(String productName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        Page<SmsHomeNewProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeNewProduct> wrapper = new LambdaQueryWrapper<>();
        if(!StrUtil.isEmpty(productName)){
            wrapper.like(SmsHomeNewProduct::getProductName, productName);
        }
        if(recommendStatus!=null){
            wrapper.eq(SmsHomeNewProduct::getRecommendStatus, recommendStatus);
        }
        wrapper.orderByDesc(SmsHomeNewProduct::getSort);
        return homeNewProductMapper.selectPage(page, wrapper);
    }
}
