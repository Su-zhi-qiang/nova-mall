package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.UmsResourceCategoryMapper;
import com.su.mall.model.UmsResourceCategory;
import com.su.mall.service.UmsResourceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 后台资源分类管理Service实现类
 * @author Su
 */
@Service
public class UmsResourceCategoryServiceImpl implements UmsResourceCategoryService {
    @Autowired
    private UmsResourceCategoryMapper resourceCategoryMapper;

    @Override
    public List<UmsResourceCategory> listAll() {
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        return resourceCategoryMapper.selectList(new LambdaQueryWrapper<UmsResourceCategory>().orderByDesc(UmsResourceCategory::getSort));
    }

    @Override
    public int create(UmsResourceCategory umsResourceCategory) {
        umsResourceCategory.setCreateTime(new Date());
        return resourceCategoryMapper.insert(umsResourceCategory);
    }

    @Override
    public int update(Long id, UmsResourceCategory umsResourceCategory) {
        umsResourceCategory.setId(id);
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        return resourceCategoryMapper.updateById(umsResourceCategory);
    }

    @Override
    public int delete(Long id) {
        // ✅ 改造：deleteByPrimaryKey → deleteById
        return resourceCategoryMapper.deleteById(id);
    }
}
