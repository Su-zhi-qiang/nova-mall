package com.su.mall.service;

import com.su.mall.model.CmsPrefrenceArea;

import java.util.List;

/**
 * 优选专区管理Service
 * @author Su
 */
public interface CmsPrefrenceAreaService {
    /**
     * 获取所有优选专区
     */
    List<CmsPrefrenceArea> listAll();
}
