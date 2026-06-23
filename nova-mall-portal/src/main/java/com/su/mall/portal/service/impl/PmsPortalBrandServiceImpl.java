package com.su.mall.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;
import com.su.mall.portal.dao.HomeDao;
import com.su.mall.portal.service.PmsPortalBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 前台品牌管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class PmsPortalBrandServiceImpl implements PmsPortalBrandService {
    private final HomeDao homeDao;
    private final PmsBrandMapper brandMapper;
    private final PmsProductMapper productMapper;

    @Override
    public Page<PmsBrand> list(Integer pageNum, Integer pageSize) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        Page<PmsBrand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsBrand> wrapper = new LambdaQueryWrapper<>();
        return brandMapper.selectPage(page, wrapper);
    }

    @Override
    public PmsBrand detail(Long brandId) {
        return brandMapper.selectById(brandId);
    }

    @Override
    public Page<PmsProduct> productList(Long brandId, Integer pageNum, Integer pageSize) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        return productMapper.selectPage(page,
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(PmsProduct::getDeleteStatus, 0)
                        .eq(PmsProduct::getPublishStatus, 1)
                        .eq(PmsProduct::getBrandId, brandId)
                        .orderByDesc(PmsProduct::getId));
    }
}
