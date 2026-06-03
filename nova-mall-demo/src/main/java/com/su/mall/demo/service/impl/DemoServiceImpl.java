package com.su.mall.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.demo.dto.PmsBrandDto;
import com.su.mall.demo.service.DemoService;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.model.PmsBrand;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DemoService实现类
 * @author Su
 */
@Service
public class DemoServiceImpl implements DemoService {
    @Autowired
    private PmsBrandMapper brandMapper;

    @Override
    public List<PmsBrand> listAllBrand() {
        return brandMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public int createBrand(PmsBrandDto pmsBrandDto) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandDto,pmsBrand);
        return brandMapper.insert(pmsBrand);
    }

    @Override
    public int updateBrand(Long id, PmsBrandDto pmsBrandDto) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandDto,pmsBrand);
        pmsBrand.setId(id);
        return brandMapper.updateById(pmsBrand);
    }

    @Override
    public int deleteBrand(Long id) {
        return brandMapper.deleteById(id);
    }

    @Override
    public List<PmsBrand> listBrand(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return brandMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public PmsBrand getBrand(Long id) {
        return brandMapper.selectById(id);
    }
}
