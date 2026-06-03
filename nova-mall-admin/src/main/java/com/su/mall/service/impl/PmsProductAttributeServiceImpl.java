package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.dao.PmsProductAttributeDao;
import com.su.mall.dto.PmsProductAttributeParam;
import com.su.mall.dto.ProductAttrInfo;
import com.su.mall.mapper.PmsProductAttributeCategoryMapper;
import com.su.mall.mapper.PmsProductAttributeMapper;
import com.su.mall.model.PmsProductAttribute;
import com.su.mall.model.PmsProductAttributeCategory;
import com.su.mall.service.PmsProductAttributeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品属性管理Service实现类
 * @author Su
 */
@Service
public class PmsProductAttributeServiceImpl implements PmsProductAttributeService {
    @Autowired
    private PmsProductAttributeMapper productAttributeMapper;
    @Autowired
    private PmsProductAttributeCategoryMapper productAttributeCategoryMapper;
    @Autowired
    private PmsProductAttributeDao productAttributeDao;

    @Override
    public List<PmsProductAttribute> getList(Long cid, Integer type, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        // ✅ 改造：LambdaQueryWrapper 替代 Example
        return productAttributeMapper.selectList(
            new LambdaQueryWrapper<PmsProductAttribute>()
                .eq(PmsProductAttribute::getProductAttributeCategoryId, cid)
                .eq(PmsProductAttribute::getType, type)
                .orderByDesc(PmsProductAttribute::getSort)
        );
    }

    @Override
    public int create(PmsProductAttributeParam pmsProductAttributeParam) {
        PmsProductAttribute pmsProductAttribute = new PmsProductAttribute();
        BeanUtils.copyProperties(pmsProductAttributeParam, pmsProductAttribute);
        // ✅ 改造：insert 替代 insertSelective
        int count = productAttributeMapper.insert(pmsProductAttribute);
        //新增商品属性以后需要更新商品属性分类数量
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        PmsProductAttributeCategory pmsProductAttributeCategory = productAttributeCategoryMapper.selectById(pmsProductAttribute.getProductAttributeCategoryId());
        if(pmsProductAttribute.getType()==0){
            pmsProductAttributeCategory.setAttributeCount(pmsProductAttributeCategory.getAttributeCount()+1);
        }else if(pmsProductAttribute.getType()==1){
            pmsProductAttributeCategory.setParamCount(pmsProductAttributeCategory.getParamCount()+1);
        }
        // ✅ 改造：updateById 替代 updateByPrimaryKey
        productAttributeCategoryMapper.updateById(pmsProductAttributeCategory);
        return count;
    }

    @Override
    public int update(Long id, PmsProductAttributeParam productAttributeParam) {
        PmsProductAttribute pmsProductAttribute = new PmsProductAttribute();
        pmsProductAttribute.setId(id);
        BeanUtils.copyProperties(productAttributeParam, pmsProductAttribute);
        // ✅ 改造：updateById 替代 updateByPrimaryKeySelective
        return productAttributeMapper.updateById(pmsProductAttribute);
    }

    @Override
    public PmsProductAttribute getItem(Long id) {
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        return productAttributeMapper.selectById(id);
    }

    @Override
    public int delete(List<Long> ids) {
        //获取分类
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        PmsProductAttribute pmsProductAttribute = productAttributeMapper.selectById(ids.get(0));
        Integer type = pmsProductAttribute.getType();
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        PmsProductAttributeCategory pmsProductAttributeCategory = productAttributeCategoryMapper.selectById(pmsProductAttribute.getProductAttributeCategoryId());
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        int count = productAttributeMapper.delete(
            new LambdaQueryWrapper<PmsProductAttribute>().in(PmsProductAttribute::getId, ids)
        );
        //删除完成后修改数量
        if(type==0){
            if(pmsProductAttributeCategory.getAttributeCount()>=count){
                pmsProductAttributeCategory.setAttributeCount(pmsProductAttributeCategory.getAttributeCount()-count);
            }else{
                pmsProductAttributeCategory.setAttributeCount(0);
            }
        }else if(type==1){
            if(pmsProductAttributeCategory.getParamCount()>=count){
                pmsProductAttributeCategory.setParamCount(pmsProductAttributeCategory.getParamCount()-count);
            }else{
                pmsProductAttributeCategory.setParamCount(0);
            }
        }
        // ✅ 改造：updateById 替代 updateByPrimaryKey
        productAttributeCategoryMapper.updateById(pmsProductAttributeCategory);
        return count;
    }

    @Override
    public List<ProductAttrInfo> getProductAttrInfo(Long productCategoryId) {
        return productAttributeDao.getProductAttrInfo(productCategoryId);
    }
}
