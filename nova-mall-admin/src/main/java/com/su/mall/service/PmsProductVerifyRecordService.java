package com.su.mall.service;


import java.util.Map;

/**
 * 商品审核记录Service
 * 
 */

public interface PmsProductVerifyRecordService {
    /**
     * 获取商品审核详情
     */
    Map<String, Object> getVerifyInfo(Long productId);

    /**
     * 更新商品审核状态
     */
    void updateVerifyStatus(Long productId, Integer verifyStatus, String verifyRemark);
}
