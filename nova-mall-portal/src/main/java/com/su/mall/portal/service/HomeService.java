package com.su.mall.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.model.CmsSubject;
import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsProductCategory;
import com.su.mall.portal.domain.HomeContentResult;
import com.su.mall.portal.domain.HomeFlashPromotion;

import java.util.List;

/**
 * 首页内容管理Service
 * @author Su
 */
public interface HomeService {

    /**
     * 获取首页内容
     */
    HomeContentResult content();

    /**
     * 获取秒杀活动信息
     */
    HomeFlashPromotion getFlashPromotion();

    /**
     * 首页商品推荐
     */
    Page<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum);

    /**
     * 获取商品分类
     * @param parentId 0:获取一级分类；其他：获取指定二级分类
     */
    List<PmsProductCategory> getProductCateList(Long parentId);

    /**
     * 根据专题分类分页获取专题
     * @param cateId 专题分类id
     */
    Page<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum);

    /**
     * 分页获取人气推荐商品
     */
    Page<PmsProduct> hotProductList(Integer pageNum, Integer pageSize);

    /**
     * 分页获取新品推荐商品
     */
    Page<PmsProduct> newProductList(Integer pageNum, Integer pageSize);
}