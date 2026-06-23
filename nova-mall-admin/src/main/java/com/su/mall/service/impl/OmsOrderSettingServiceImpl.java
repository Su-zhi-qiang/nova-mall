package com.su.mall.service.impl;

import com.su.mall.mapper.OmsOrderSettingMapper;
import com.su.mall.model.OmsOrderSetting;
import com.su.mall.service.OmsOrderSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 订单设置管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class OmsOrderSettingServiceImpl implements OmsOrderSettingService {
    private final OmsOrderSettingMapper orderSettingMapper;

    @Override
    public OmsOrderSetting getItem(Long id) {
        return orderSettingMapper.selectById(id);
    }

    @Override
    public int update(Long id, OmsOrderSetting orderSetting) {
        orderSetting.setId(id);
        return orderSettingMapper.updateById(orderSetting);
    }
}
