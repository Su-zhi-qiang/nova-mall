package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.PmsProductParam;
import com.su.mall.dto.PmsProductQueryParam;
import com.su.mall.dto.PmsProductResult;
import com.su.mall.model.PmsProduct;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品管理Service
 * <p>提供商品的CRUD、批量状态变更（审核/上架/推荐/新品/删除）、模糊搜索等功能
 * <p>商品创建和更新涉及多表操作（商品基本信息+SKU库存+属性值+阶梯价+满减+会员价），需事务保证
 *
 * @see PmsProductServiceImpl
 */
public interface PmsProductService {
    /**
     * 创建商品
     */
    @Transactional(isolation = Isolation.DEFAULT,propagation = Propagation.REQUIRED)
    int create(PmsProductParam productParam);

    /**
     * 根据商品ID获取商品信息（用于更新商品）
     */
    PmsProductResult getUpdateInfo(Long id);

    /**
     * 更新商品
     */
    @Transactional
    int update(Long id, PmsProductParam productParam);

    /**
     * 分页查询商品（使用 MyBatis-Plus）
     */
    Page<PmsProduct> listPage(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum);

    /**
     * 批量修改审核状态
     * @param ids 商品ID列表
     * @param verifyStatus 审核状态
     * @param detail 审核详情
     */
    @Transactional
    int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail);

    /**
     * 批量修改商品上架状态
     */
    int updatePublishStatus(List<Long> ids, Integer publishStatus);

    /**
     * 批量修改商品推荐状态
     */
    int updateRecommendStatus(List<Long> ids, Integer recommendStatus);

    /**
     * 批量修改新品状态
     */
    int updateNewStatus(List<Long> ids, Integer newStatus);

    /**
     * 批量删除商品
     */
    int updateDeleteStatus(List<Long> ids, Integer deleteStatus);

    /**
     * 根据商品名称或货号模糊查询
     */
    List<PmsProduct> list(String keyword);
}
