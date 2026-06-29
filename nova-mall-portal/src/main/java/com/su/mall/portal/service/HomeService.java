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
 * <p>聚合首页所有展示模块的数据，包括广告、品牌、秒杀、推荐商品和专题
 *
 * @see com.su.mall.portal.controller.HomeController
 */
public interface HomeService {

    /**
     * 获取首页完整内容（聚合所有模块数据）
     *
     * @return 包含广告、品牌、秒杀、推荐商品、专题的聚合结果
     */
    HomeContentResult content();

    /**
     * 获取当前秒杀场次信息（含商品列表和下一场次时间）
     *
     * @return 秒杀场次信息
     */
    HomeFlashPromotion getFlashPromotion();

    /**
     * 分页获取推荐商品列表
     *
     * @param pageSize 每页数量
     * @param pageNum  页码
     * @return 推荐商品分页结果
     */
    Page<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum);

    /**
     * 获取商品分类列表
     *
     * @param parentId 父分类ID：0=获取一级分类，其他=获取指定分类的子分类
     * @return 商品分类列表
     */
    List<PmsProductCategory> getProductCateList(Long parentId);

    /**
     * 分页获取专题列表
     *
     * @param cateId   专题分类ID（可选）
     * @param pageSize 每页数量
     * @param pageNum  页码
     * @return 专题分页结果
     */
    Page<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum);

    /**
     * 分页获取人气推荐商品
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 人气商品分页结果
     */
    Page<PmsProduct> hotProductList(Integer pageNum, Integer pageSize);

    /**
     * 分页获取新品推荐商品
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 新品分页结果
     */
    Page<PmsProduct> newProductList(Integer pageNum, Integer pageSize);
}
