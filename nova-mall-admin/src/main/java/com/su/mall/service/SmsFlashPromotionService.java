package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.model.SmsFlashPromotion;

import java.util.List;

/**
 * 限时购（秒杀）活动管理Service
 * <p>提供秒杀活动的创建、修改、删除、上下线状态变更等后台管理功能
 *
 * @see SmsFlashPromotionServiceImpl
 */
public interface SmsFlashPromotionService {
    /**
     * 添加活动
     */
    int create(SmsFlashPromotion flashPromotion);

    /**
     * 修改指定活动
     */
    int update(Long id, SmsFlashPromotion flashPromotion);

    /**
     * 删除单个活动
     */
    int delete(Long id);

    /**
     * 修改上下线状态
     */
    int updateStatus(Long id, Integer status);

    /**
     * 获取活动详情
     */
    SmsFlashPromotion getItem(Long id);

    /**
     * 分页查询活动
     */
    Page<SmsFlashPromotion> list(String keyword, Integer pageSize, Integer pageNum);
}
