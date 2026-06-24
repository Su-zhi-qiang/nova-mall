package com.su.mall.portal.service;

import com.su.mall.portal.domain.OmsOrderCommentParam;

import java.util.List;
import java.util.Map;

/**
 * 商品评价Service
 */
public interface OmsOrderCommentService {
    /**
     * 提交商品评价
     */
    int create(OmsOrderCommentParam param);

    /**
     * 获取商品评价列表
     */
    Map<String, Object> listByProductId(Long productId);

    /**
     * 获取用户的评价历史列表
     */
    List<Map<String, Object>> listByMemberId();
}
