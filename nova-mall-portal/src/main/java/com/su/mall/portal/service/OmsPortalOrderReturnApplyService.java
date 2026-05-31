package com.su.mall.portal.service;

import com.su.mall.portal.domain.OmsOrderReturnApplyParam;

/**
 * 前台订单退货管理Service
 * @author Su
 */
public interface OmsPortalOrderReturnApplyService {
    /**
     * 提交申请
     */
    int create(OmsOrderReturnApplyParam returnApply);
}
