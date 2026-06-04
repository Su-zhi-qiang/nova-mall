package com.su.mall.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;

import java.util.List;

/**
 * 前台品牌管理Service
 * @author Su
 */
public interface PmsPortalBrandService {
    /**
     * 分页获取推荐品牌
     */
    Page<PmsBrand> list(Integer pageNum, Integer pageSize);

    /**
     * 获取品牌详情
     */
    PmsBrand detail(Long brandId);

    /**
     * 分页获取品牌关联商品
     */
    Page<PmsProduct> productList(Long brandId, Integer pageNum, Integer pageSize);
}
