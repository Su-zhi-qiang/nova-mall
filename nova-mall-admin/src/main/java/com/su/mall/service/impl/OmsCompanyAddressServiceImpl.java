package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.OmsCompanyAddressMapper;
import com.su.mall.model.OmsCompanyAddress;
import com.su.mall.service.OmsCompanyAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收货地址管理Service实现类
 * @author Su
 */
@Service
public class OmsCompanyAddressServiceImpl implements OmsCompanyAddressService {
    @Autowired
    private OmsCompanyAddressMapper companyAddressMapper;
    
    @Override
    public List<OmsCompanyAddress> list() {
        // ✅ 改造：selectList 替代 selectByExample
        return companyAddressMapper.selectList(new LambdaQueryWrapper<OmsCompanyAddress>());
    }
}
