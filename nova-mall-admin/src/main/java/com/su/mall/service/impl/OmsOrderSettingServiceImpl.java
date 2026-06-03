package com.su.mall.service.impl;

import com.su.mall.mapper.OmsOrderSettingMapper;
import com.su.mall.model.OmsOrderSetting;
import com.su.mall.service.OmsOrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单设置管理Service实现类
 * @author Su
 */
@Service
public class OmsOrderSettingServiceImpl implements OmsOrderSettingService {
    @Autowired
    private OmsOrderSettingMapper orderSettingMapper;

    @Override
    public OmsOrderSetting getItem(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return orderSettingMapper.selectById(id);
    }

    @Override
    public int update(Long id, OmsOrderSetting orderSetting) {
        orderSetting.setId(id);
        // ✅ 改造：updateByPrimaryKey → updateById
        return orderSettingMapper.updateById(orderSetting);
    }
}
