package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.service.SmsCouponHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 优惠券领取记录管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsCouponHistoryServiceImpl implements SmsCouponHistoryService {
    private final SmsCouponHistoryMapper historyMapper;
    @Override
    public Page<SmsCouponHistory> list(Long couponId, Integer useStatus, String orderSn, Integer pageSize, Integer pageNum) {
        Page<SmsCouponHistory> page = new Page<>(pageNum, pageSize);
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
        return historyMapper.selectPage(page, wrapper);
    }
}
