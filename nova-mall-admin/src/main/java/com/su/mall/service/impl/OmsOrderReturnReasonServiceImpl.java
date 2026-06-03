package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.mapper.OmsOrderReturnReasonMapper;
import com.su.mall.model.OmsOrderReturnReason;
import com.su.mall.service.OmsOrderReturnReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 订单原因管理Service实现类
 *
 * @author Su
 */
@Service
public class OmsOrderReturnReasonServiceImpl implements OmsOrderReturnReasonService {
    @Autowired
    private OmsOrderReturnReasonMapper returnReasonMapper;
    @Override
    public int create(OmsOrderReturnReason returnReason) {
        returnReason.setCreateTime(new Date());
        // ✅ 改造：insertSelective → insert
        return returnReasonMapper.insert(returnReason);
    }

    @Override
    public int update(Long id, OmsOrderReturnReason returnReason) {
        returnReason.setId(id);
        // ✅ 改造：updateByPrimaryKey → updateById
        return returnReasonMapper.updateById(returnReason);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → deleteByIds
        return returnReasonMapper.deleteByIds(ids);
    }

    @Override
    public List<OmsOrderReturnReason> list(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        // ✅ 改造：selectByExample → selectList
        LambdaQueryWrapper<OmsOrderReturnReason> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OmsOrderReturnReason::getSort);
        return returnReasonMapper.selectList(wrapper);
    }

    @Override
    public int updateStatus(List<Long> ids, Integer status) {
        if(!status.equals(0)&&!status.equals(1)){
            return 0;
        }
        OmsOrderReturnReason record = new OmsOrderReturnReason();
        record.setStatus(status);
        // ✅ 改造：updateByExampleSelective → update + LambdaUpdateWrapper
        return returnReasonMapper.update(record, new LambdaUpdateWrapper<OmsOrderReturnReason>()
                .in(OmsOrderReturnReason::getId, ids));
    }

    @Override
    public OmsOrderReturnReason getItem(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return returnReasonMapper.selectById(id);
    }
}
