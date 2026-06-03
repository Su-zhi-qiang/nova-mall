package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.service.SmsCouponHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 优惠券领取记录管理Service实现类
 * @author Su
 */
@Service
public class SmsCouponHistoryServiceImpl implements SmsCouponHistoryService {
    @Autowired
    private SmsCouponHistoryMapper historyMapper;
    @Override
    public List<SmsCouponHistory> list(Long couponId, Integer useStatus, String orderSn, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<>())
        LambdaQueryWrapper<SmsCouponHistory> wrapper = new LambdaQueryWrapper<>();
        if(couponId!=null){
            wrapper.eq(SmsCouponHistory::getCouponId, couponId);
        }
        if(useStatus!=null){
            wrapper.eq(SmsCouponHistory::getUseStatus, useStatus);
        }
        if(!StrUtil.isEmpty(orderSn)){
            wrapper.eq(SmsCouponHistory::getOrderSn, orderSn);
        }
        return historyMapper.selectList(wrapper);
    }
}
