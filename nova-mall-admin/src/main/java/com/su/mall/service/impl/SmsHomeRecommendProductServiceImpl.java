package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeRecommendProductMapper;
import com.su.mall.model.SmsHomeRecommendProduct;
import com.su.mall.service.SmsHomeRecommendProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页人气推荐管理Service实现类
 *
 * @author Su
 */
@Service
public class SmsHomeRecommendProductServiceImpl implements SmsHomeRecommendProductService {
    @Autowired
    private SmsHomeRecommendProductMapper recommendProductMapper;
    @Override
    public int create(List<SmsHomeRecommendProduct> homeRecommendProductList) {
        for (SmsHomeRecommendProduct recommendProduct : homeRecommendProductList) {
            recommendProduct.setRecommendStatus(1);
            recommendProduct.setSort(0);
            recommendProductMapper.insert(recommendProduct);
        }
        return homeRecommendProductList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        SmsHomeRecommendProduct recommendProduct = new SmsHomeRecommendProduct();
        recommendProduct.setId(id);
        recommendProduct.setSort(sort);
        return recommendProductMapper.updateById(recommendProduct);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → delete(new LambdaQueryWrapper<>())
        return recommendProductMapper.delete(new LambdaQueryWrapper<SmsHomeRecommendProduct>()
                .in(SmsHomeRecommendProduct::getId, ids));
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        // ✅ 改造：updateByExampleSelective → update(new LambdaQueryWrapper<>().in(...))
        SmsHomeRecommendProduct record = new SmsHomeRecommendProduct();
        record.setRecommendStatus(recommendStatus);
        return recommendProductMapper.update(record, new LambdaQueryWrapper<SmsHomeRecommendProduct>()
                .in(SmsHomeRecommendProduct::getId, ids));
    }

    @Override
    public Page<SmsHomeRecommendProduct> list(String productName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        Page<SmsHomeRecommendProduct> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<>())
        LambdaQueryWrapper<SmsHomeRecommendProduct> wrapper = new LambdaQueryWrapper<>();
        if(!StrUtil.isEmpty(productName)){
            wrapper.like(SmsHomeRecommendProduct::getProductName, productName);
        }
        if(recommendStatus!=null){
            wrapper.eq(SmsHomeRecommendProduct::getRecommendStatus, recommendStatus);
        }
        wrapper.orderByDesc(SmsHomeRecommendProduct::getSort);
        return recommendProductMapper.selectPage(page, wrapper);
    }
}
