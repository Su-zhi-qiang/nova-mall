package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.UmsResourceMapper;
import com.su.mall.model.UmsResource;
import com.su.mall.service.UmsAdminCacheService;
import com.su.mall.service.UmsResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 后台资源管理Service实现类
 * @author Su
 */
@Service
public class UmsResourceServiceImpl implements UmsResourceService {
    @Autowired
    private UmsResourceMapper resourceMapper;
    @Autowired
    private UmsAdminCacheService adminCacheService;
    @Override
    public int create(UmsResource umsResource) {
        umsResource.setCreateTime(new Date());
        return resourceMapper.insert(umsResource);
    }

    @Override
    public int update(Long id, UmsResource umsResource) {
        umsResource.setId(id);
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        int count = resourceMapper.updateById(umsResource);
        adminCacheService.delResourceListByResource(id);
        return count;
    }

    @Override
    public UmsResource getItem(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return resourceMapper.selectById(id);
    }

    @Override
    public int delete(Long id) {
        // ✅ 改造：deleteByPrimaryKey → deleteById
        int count = resourceMapper.deleteById(id);
        adminCacheService.delResourceListByResource(id);
        return count;
    }

    @Override
    public Page<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageSize, Integer pageNum) {
        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        LambdaQueryWrapper<UmsResource> wrapper = new LambdaQueryWrapper<>();
        if(categoryId!=null){
            wrapper.eq(UmsResource::getCategoryId, categoryId);
        }
        if(StrUtil.isNotEmpty(nameKeyword)){
            wrapper.like(UmsResource::getName, nameKeyword);
        }
        if(StrUtil.isNotEmpty(urlKeyword)){
            wrapper.like(UmsResource::getUrl, urlKeyword);
        }
        return resourceMapper.selectPage(page, wrapper);
    }

    @Override
    public List<UmsResource> listAll() {
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        return resourceMapper.selectList(new LambdaQueryWrapper<>());
    }
}
