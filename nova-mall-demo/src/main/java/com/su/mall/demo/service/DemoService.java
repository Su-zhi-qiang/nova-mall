package com.su.mall.demo.service;

import com.su.mall.demo.dto.PmsBrandDto;
import com.su.mall.model.PmsBrand;

import java.util.List;

/**
 * DemoService接口
 * @author Su
 */
public interface DemoService {
    List<PmsBrand> listAllBrand();

    int createBrand(PmsBrandDto pmsBrandDto);

    int updateBrand(Long id, PmsBrandDto pmsBrandDto);

    int deleteBrand(Long id);

    List<PmsBrand> listBrand(int pageNum, int pageSize);

    PmsBrand getBrand(Long id);
}
