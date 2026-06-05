package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.OmsOrderReturnApplyDao;
import com.su.mall.dto.OmsOrderReturnApplyResult;
import com.su.mall.dto.OmsReturnApplyQueryParam;
import com.su.mall.dto.OmsUpdateStatusParam;
import com.su.mall.mapper.OmsOrderReturnApplyMapper;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.service.OmsOrderReturnApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 订单退货管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class OmsOrderReturnApplyServiceImpl implements OmsOrderReturnApplyService {
    private final OmsOrderReturnApplyDao returnApplyDao;
    private final OmsOrderReturnApplyMapper returnApplyMapper;
    @Override
    public Page<OmsOrderReturnApply> list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum) {
        Page<OmsOrderReturnApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsOrderReturnApply> wrapper = new LambdaQueryWrapper<>();
        return returnApplyMapper.selectPage(page, wrapper);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → delete + LambdaQueryWrapper
        return returnApplyMapper.delete(new LambdaQueryWrapper<OmsOrderReturnApply>()
                .eq(OmsOrderReturnApply::getStatus, 3)
                .in(OmsOrderReturnApply::getId, ids));
    }

    @Override
    public int updateStatus(Long id, OmsUpdateStatusParam statusParam) {
        Integer status = statusParam.getStatus();
        OmsOrderReturnApply returnApply = new OmsOrderReturnApply();
        if(status.equals(1)){
            //确认退货
            returnApply.setId(id);
            returnApply.setStatus(1);
            returnApply.setReturnAmount(statusParam.getReturnAmount());
            returnApply.setCompanyAddressId(statusParam.getCompanyAddressId());
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else if(status.equals(2)){
            //完成退货
            returnApply.setId(id);
            returnApply.setStatus(2);
            returnApply.setReceiveTime(new Date());
            returnApply.setReceiveMan(statusParam.getReceiveMan());
            returnApply.setReceiveNote(statusParam.getReceiveNote());
        }else if(status.equals(3)){
            //拒绝退货
            returnApply.setId(id);
            returnApply.setStatus(3);
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else{
            return 0;
        }
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        return returnApplyMapper.updateById(returnApply);
    }

    @Override
    public OmsOrderReturnApplyResult getItem(Long id) {
        return returnApplyDao.getDetail(id);
    }
}
