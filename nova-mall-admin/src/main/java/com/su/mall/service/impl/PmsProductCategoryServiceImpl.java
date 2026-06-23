package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.PmsProductCategoryAttributeRelationDao;
import com.su.mall.dao.PmsProductCategoryDao;
import com.su.mall.dto.PmsProductCategoryParam;
import com.su.mall.dto.PmsProductCategoryWithChildrenItem;
import com.su.mall.mapper.PmsProductCategoryAttributeRelationMapper;
import com.su.mall.mapper.PmsProductCategoryMapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.model.*;
import com.su.mall.service.PmsProductCategoryService;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class PmsProductCategoryServiceImpl implements PmsProductCategoryService {
    private final PmsProductCategoryMapper productCategoryMapper;
    private final PmsProductMapper productMapper;
    private final PmsProductCategoryAttributeRelationDao productCategoryAttributeRelationDao;
    private final PmsProductCategoryAttributeRelationMapper productCategoryAttributeRelationMapper;
    private final PmsProductCategoryDao productCategoryDao;
    
    @Override
    public int create(PmsProductCategoryParam pmsProductCategoryParam) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setProductCount(0);
        BeanUtils.copyProperties(pmsProductCategoryParam, productCategory);
        //没有父分类时为一级分类
        setCategoryLevel(productCategory);
        int count = productCategoryMapper.insert(productCategory);
        //创建筛选属性关联
        List<Long> productAttributeIdList = pmsProductCategoryParam.getProductAttributeIdList();
        if(!CollUtil.isEmpty(productAttributeIdList)){
            insertRelationList(productCategory.getId(), productAttributeIdList);
        }
        return count;
    }

    /**
     * 批量插入商品分类与筛选属性关系表
     * @param productCategoryId 商品分类id
     * @param productAttributeIdList 相关商品筛选属性id集合
     */
    private void insertRelationList(Long productCategoryId, List<Long> productAttributeIdList) {
        List<PmsProductCategoryAttributeRelation> relationList = new ArrayList<>();
        for (Long productAttrId : productAttributeIdList) {
            PmsProductCategoryAttributeRelation relation = new PmsProductCategoryAttributeRelation();
            relation.setProductAttributeId(productAttrId);
            relation.setProductCategoryId(productCategoryId);
            relationList.add(relation);
        }
        productCategoryAttributeRelationDao.insertList(relationList);
    }

    @Override
    public int update(Long id, PmsProductCategoryParam pmsProductCategoryParam) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setId(id);
        BeanUtils.copyProperties(pmsProductCategoryParam, productCategory);
        setCategoryLevel(productCategory);
        //更新商品分类时要更新商品中的名称
        PmsProduct product = new PmsProduct();
        product.setProductCategoryName(productCategory.getName());
        LambdaQueryWrapper<PmsProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(PmsProduct::getProductCategoryId, id);
        productMapper.update(product, productWrapper);
        //同时更新筛选属性的信息
        if(!CollUtil.isEmpty(pmsProductCategoryParam.getProductAttributeIdList())){
            productCategoryAttributeRelationMapper.delete(
                new LambdaQueryWrapper<PmsProductCategoryAttributeRelation>()
                    .eq(PmsProductCategoryAttributeRelation::getProductCategoryId, id)
            );
            insertRelationList(id,pmsProductCategoryParam.getProductAttributeIdList());
        }else{
            productCategoryAttributeRelationMapper.delete(
                new LambdaQueryWrapper<PmsProductCategoryAttributeRelation>()
                    .eq(PmsProductCategoryAttributeRelation::getProductCategoryId, id)
            );
        }
        return productCategoryMapper.updateById(productCategory);
    }

    @Override
    public Page<PmsProductCategory> getList(Long parentId, Integer pageSize, Integer pageNum) {
        Page<PmsProductCategory> page = new Page<>(pageNum, pageSize);
        return productCategoryMapper.selectPage(
            page,
            new LambdaQueryWrapper<PmsProductCategory>()
                .eq(PmsProductCategory::getParentId, parentId)
                .orderByDesc(PmsProductCategory::getSort)
        );
    }

    @Override
    public int delete(Long id) {
        return productCategoryMapper.deleteById(id);
    }

    @Override
    public PmsProductCategory getItem(Long id) {
        return productCategoryMapper.selectById(id);
    }

    @Override
    public int updateNavStatus(List<Long> ids, Integer navStatus) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setNavStatus(navStatus);
        return productCategoryMapper.update(
            productCategory,
            new LambdaQueryWrapper<PmsProductCategory>().in(PmsProductCategory::getId, ids)
        );
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setShowStatus(showStatus);
        return productCategoryMapper.update(
            productCategory,
            new LambdaQueryWrapper<PmsProductCategory>().in(PmsProductCategory::getId, ids)
        );
    }

    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        return productCategoryDao.listWithChildren();
    }

    /**
     * 根据分类的parentId设置分类的level
     */
    private void setCategoryLevel(PmsProductCategory productCategory) {
        //没有父分类时为一级分类
        if (productCategory.getParentId() == 0) {
            productCategory.setLevel(0);
        } else {
            //有父分类时选择根据父分类level设置
            PmsProductCategory parentCategory = productCategoryMapper.selectById(productCategory.getParentId());
            if (parentCategory != null) {
                productCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
                productCategory.setLevel(0);
            }
        }
    }
}
