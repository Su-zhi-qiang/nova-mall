package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.dao.PmsProductAttributeCategoryDao;
import com.su.mall.dto.PmsProductAttributeCategoryItem;
import com.su.mall.mapper.PmsProductAttributeCategoryMapper;
import com.su.mall.model.PmsProductAttributeCategory;
import com.su.mall.service.PmsProductAttributeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品属性分类管理Service实现类
 * @author Su
 */
@Service
public class PmsProductAttributeCategoryServiceImpl implements PmsProductAttributeCategoryService {
    @Autowired
    private PmsProductAttributeCategoryMapper productAttributeCategoryMapper;
    @Autowired
    private PmsProductAttributeCategoryDao productAttributeCategoryDao;

    @Override
    public int create(String name) {
        PmsProductAttributeCategory productAttributeCategory = new PmsProductAttributeCategory();
        productAttributeCategory.setName(name);
        // ✅ 改造：insert 替代 insertSelective
        return productAttributeCategoryMapper.insert(productAttributeCategory);
    }

    @Override
    public int update(Long id, String name) {
        PmsProductAttributeCategory productAttributeCategory = new PmsProductAttributeCategory();
        productAttributeCategory.setName(name);
        productAttributeCategory.setId(id);
        // ✅ 改造：updateById 替代 updateByPrimaryKeySelective
        return productAttributeCategoryMapper.updateById(productAttributeCategory);
    }

    @Override
    public int delete(Long id) {
        // ✅ 改造：deleteById 替代 deleteByPrimaryKey
        return productAttributeCategoryMapper.deleteById(id);
    }

    @Override
    public PmsProductAttributeCategory getItem(Long id) {
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        return productAttributeCategoryMapper.selectById(id);
    }

    @Override
    public List<PmsProductAttributeCategory> getList(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        // ✅ 改造：selectList 替代 selectByExample
        return productAttributeCategoryMapper.selectList(new LambdaQueryWrapper<PmsProductAttributeCategory>());
    }

    @Override
    public List<PmsProductAttributeCategoryItem> getListWithAttr() {
        return productAttributeCategoryDao.getListWithAttr();
    }
}
