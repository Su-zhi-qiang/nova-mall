package com.su.mall.portal.service.impl;

import com.su.mall.mapper.OmsOrderReturnApplyMapper;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.portal.domain.OmsOrderReturnApplyParam;
import com.su.mall.portal.service.OmsPortalOrderReturnApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 订单退货管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class OmsPortalOrderReturnApplyServiceImpl implements OmsPortalOrderReturnApplyService {
    private final OmsOrderReturnApplyMapper returnApplyMapper;
    @Override
    public int create(OmsOrderReturnApplyParam returnApply) {
        OmsOrderReturnApply realApply = new OmsOrderReturnApply();
        BeanUtils.copyProperties(returnApply,realApply);
        realApply.setCreateTime(new Date());
        realApply.setStatus(0);
        return returnApplyMapper.insert(realApply);
    }
}
