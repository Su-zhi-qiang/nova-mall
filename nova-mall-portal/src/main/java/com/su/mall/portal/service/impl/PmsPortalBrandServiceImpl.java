package com.su.mall.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.common.api.CommonPage;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;
import com.su.mall.portal.dao.HomeDao;
import com.su.mall.portal.service.PmsPortalBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 前台品牌管理Service实现类
 * @author Su
 */
@Service
public class PmsPortalBrandServiceImpl implements PmsPortalBrandService {
    @Autowired
    private HomeDao homeDao;
    @Autowired
    private PmsBrandMapper brandMapper;
    @Autowired
    private PmsProductMapper productMapper;

    @Override
    public List<PmsBrand> recommendList(Integer pageNum, Integer pageSize) {
        // 确保 pageNum 至少为 1
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int offset = (pageNum - 1) * pageSize;
        return homeDao.getRecommendBrandList(offset, pageSize);
    }

    @Override
    public PmsBrand detail(Long brandId) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return brandMapper.selectById(brandId);
    }

    @Override
    public CommonPage<PmsProduct> productList(Long brandId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProduct>())
        List<PmsProduct> productList = productMapper.selectList(
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(PmsProduct::getDeleteStatus, 0)
                        .eq(PmsProduct::getPublishStatus, 1)
                        .eq(PmsProduct::getBrandId, brandId)
                        .orderByDesc(PmsProduct::getId));
        return CommonPage.restPage(productList);
    }
}
