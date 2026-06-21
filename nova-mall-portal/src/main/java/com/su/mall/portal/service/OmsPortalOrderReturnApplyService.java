package com.su.mall.portal.service;

import com.su.mall.common.api.CommonPage;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.portal.domain.OmsOrderReturnApplyParam;

import java.util.Map;

/**
 * 前台订单退货管理Service
 * @author Su
 */
public interface OmsPortalOrderReturnApplyService {
    /**
     * 提交申请
     */
    int create(OmsOrderReturnApplyParam returnApply);

    /**
     * 查询当前用户的退货申请列表
     */
    CommonPage<OmsOrderReturnApply> list(Integer pageNum, Integer pageSize);

    /**
     * 查询订单是否已有退货申请
     */
    Map<Long, Integer> getReturnStatusByOrderIds(Long memberId);

    /**
     * 软删除退货申请
     */
    int delete(Long id);
}
