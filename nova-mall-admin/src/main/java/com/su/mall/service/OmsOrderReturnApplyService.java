package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.OmsOrderReturnApplyResult;
import com.su.mall.dto.OmsReturnApplyQueryParam;
import com.su.mall.dto.OmsUpdateStatusParam;
import com.su.mall.model.OmsOrderReturnApply;

import java.util.List;

/**
 * 退货申请管理Service
 * 
 */
public interface OmsOrderReturnApplyService {
    /**
     * 分页查询申请
     */
    Page<OmsOrderReturnApply> list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum);

    /**
     * 批量删除申请
     */
    int delete(List<Long> ids);

    /**
     * 修改指定申请状态
     */
    int updateStatus(Long id, OmsUpdateStatusParam statusParam);

    /**
     * 获取指定申请详情
     */
    OmsOrderReturnApplyResult getItem(Long id);
}
