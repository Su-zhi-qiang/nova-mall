package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeNewProductMapper;
import com.su.mall.model.SmsHomeNewProduct;
import com.su.mall.service.SmsHomeNewProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页新品推荐管理Service实现类
 *
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsHomeNewProductServiceImpl implements SmsHomeNewProductService {
    private final SmsHomeNewProductMapper homeNewProductMapper;
    @Override
    public int create(List<SmsHomeNewProduct> homeNewProductList) {
        for (SmsHomeNewProduct SmsHomeNewProduct : homeNewProductList) {
            SmsHomeNewProduct.setRecommendStatus(1);
            SmsHomeNewProduct.setSort(0);
            homeNewProductMapper.insert(SmsHomeNewProduct);
        }
        return homeNewProductList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        SmsHomeNewProduct homeNewProduct = new SmsHomeNewProduct();
        homeNewProduct.setId(id);
        homeNewProduct.setSort(sort);
        return homeNewProductMapper.updateById(homeNewProduct);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → delete(new LambdaQueryWrapper<>())
        return homeNewProductMapper.delete(new LambdaQueryWrapper<SmsHomeNewProduct>()
                .in(SmsHomeNewProduct::getId, ids));
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        // ✅ 改造：updateByExampleSelective → update(new LambdaQueryWrapper<>().in(...))
        SmsHomeNewProduct record = new SmsHomeNewProduct();
        record.setRecommendStatus(recommendStatus);
        return homeNewProductMapper.update(record, new LambdaQueryWrapper<SmsHomeNewProduct>()
                .in(SmsHomeNewProduct::getId, ids));
    }

    @Override
    public Page<SmsHomeNewProduct> list(String productName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        Page<SmsHomeNewProduct> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<>())
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
