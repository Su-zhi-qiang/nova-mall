package com.su.mall.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.model.PmsProduct;
import com.su.mall.portal.domain.PmsPortalProductDetail;
import com.su.mall.portal.domain.PmsProductCategoryNode;

import java.util.List;

/**
 * 前台商品管理Service
 * @author Su
 */
public interface PmsPortalProductService {
    /**
     * 综合搜索商品
     */
    Page<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort);

    /**
     * 以树形结构获取所有商品分类
     */
    List<PmsProductCategoryNode> categoryTreeList();

    /**
     * 获取前台商品详情
     */
    PmsPortalProductDetail detail(Long id);
}
