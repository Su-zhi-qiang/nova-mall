package com.su.mall.service;

import com.su.mall.model.OmsCompanyAddress;

import java.util.List;

/**
 * 收货地址管理Service
 * 
 */
public interface OmsCompanyAddressService {
    /**
     * 获取全部收货地址
     */
    List<OmsCompanyAddress> list();
}
