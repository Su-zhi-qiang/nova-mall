package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.UmsResourceCategoryMapper;
import com.su.mall.model.UmsResourceCategory;
import com.su.mall.service.UmsResourceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 后台资源分类管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsResourceCategoryServiceImpl implements UmsResourceCategoryService {
    private final UmsResourceCategoryMapper resourceCategoryMapper;

    @Override
    public List<UmsResourceCategory> listAll() {
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
        return resourceCategoryMapper.updateById(umsResourceCategory);
    }

    @Override
    public int delete(Long id) {
        return resourceCategoryMapper.deleteById(id);
    }
}
