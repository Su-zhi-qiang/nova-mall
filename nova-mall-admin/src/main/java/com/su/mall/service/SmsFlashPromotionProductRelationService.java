package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.SmsFlashPromotionProduct;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 限时购商品关联管理Service
 * 
 */
public interface SmsFlashPromotionProductRelationService {
    /**
     * 批量添加关联
     */
    @Transactional
    int create(List<SmsFlashPromotionProductRelation> relationList);

    /**
     * 修改关联信息
     */
    int update(Long id, SmsFlashPromotionProductRelation relation);

    /**
     * 删除关联
     */
    int delete(Long id);

    /**
     * 获取关联详情
     */
    SmsFlashPromotionProductRelation getItem(Long id);

    /**
     * 分页查询相关商品及限时购促销信息（含商品详情）
     *
     * @param flashPromotionId        限时购活动id（可选）
     * @param flashPromotionSessionId 限时购场次id（可选）
     * @param pageSize                每页数量
     * @param pageNum                 页码
     */
    Page<SmsFlashPromotionProduct> list(Long flashPromotionId, Long flashPromotionSessionId, Integer pageSize, Integer pageNum);

    /**
     * 根据活动和场次id获取商品关系数量
     * @param flashPromotionId        限时购id
     * @param flashPromotionSessionId 限时购场次id
     */
    long getCount(Long flashPromotionId,Long flashPromotionSessionId);

    /**
     * 重置指定活动+场次下的秒杀库存（将 flash_promotion_count 还原为 original_count，已抢购数归零）
     */
    int resetFlashStock(Long flashPromotionId, Long flashPromotionSessionId);
}
