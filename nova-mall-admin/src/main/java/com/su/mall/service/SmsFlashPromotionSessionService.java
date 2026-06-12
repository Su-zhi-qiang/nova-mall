package com.su.mall.service;

import com.su.mall.dto.SmsFlashPromotionSessionDetail;
import com.su.mall.model.SmsFlashPromotionSession;

import java.util.List;

/**
 * 限时购场次管理Service
 * @author Su
 */
public interface SmsFlashPromotionSessionService {
    /**
     * 添加场次
     */
    int create(SmsFlashPromotionSession promotionSession);

    /**
     * 修改场次
     */
    int update(Long id, SmsFlashPromotionSession promotionSession);

    /**
     * 修改场次启用状态
     */
    int updateStatus(Long id, Integer status);

    /**
     * 删除场次
     */
    int delete(Long id);

    /**
     * 获取详情
     */
    SmsFlashPromotionSession getItem(Long id);

    /**
     * 根据启用状态获取场次列表
     */
    List<SmsFlashPromotionSession> list();

    /**
     * 获取全部可选场次及其数量
     */
    List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId);

    /**
     * 重置指定活动+场次下的秒杀库存（将 flash_promotion_count 还原为 original_count，已抢购数归零）
     */
    int resetFlashStock(Long flashPromotionId, Long flashPromotionSessionId);
}
