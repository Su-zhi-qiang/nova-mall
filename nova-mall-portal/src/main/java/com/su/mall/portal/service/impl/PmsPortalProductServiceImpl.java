package com.su.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.service.RedisService;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.portal.dao.PortalProductDao;
import com.su.mall.portal.domain.PmsPortalProductDetail;
import com.su.mall.portal.domain.PmsProductCategoryNode;
import com.su.mall.portal.service.PmsPortalProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台订单管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class PmsPortalProductServiceImpl implements PmsPortalProductService {
    private final PmsProductMapper productMapper;
    private final PmsProductCategoryMapper productCategoryMapper;
    private final PmsBrandMapper brandMapper;
    private final PmsProductAttributeMapper productAttributeMapper;
    private final PmsProductAttributeValueMapper productAttributeValueMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final PmsProductLadderMapper productLadderMapper;
    private final PmsProductFullReductionMapper productFullReductionMapper;
    private final PortalProductDao portalProductDao;
    private final RedisService redisService;

    @Override
    public Page<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort) {
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProduct>())
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<PmsProduct>()
                .eq(PmsProduct::getDeleteStatus, 0)
                .eq(PmsProduct::getPublishStatus, 1)
                .like(StrUtil.isNotEmpty(keyword), PmsProduct::getName, keyword)
                .eq(brandId != null, PmsProduct::getBrandId, brandId)
                .eq(productCategoryId != null, PmsProduct::getProductCategoryId, productCategoryId)
                .orderByDesc(sort == 1 ? PmsProduct::getId : null)
                .orderByDesc(sort == 2 ? PmsProduct::getSale : null)
                .orderByAsc(sort == 3 ? PmsProduct::getPrice : null)
                .orderByDesc(sort == 4 ? PmsProduct::getPrice : null);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public List<PmsProductCategoryNode> categoryTreeList() {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProductCategory>())
        List<PmsProductCategory> allList = productCategoryMapper.selectList(new LambdaQueryWrapper<PmsProductCategory>());
        List<PmsProductCategoryNode> result = allList.stream()
                .filter(item -> item.getParentId().equals(0L))
                .map(item -> covert(item, allList))
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public PmsPortalProductDetail detail(Long id) {
        String cacheKey = "product:detail:" + id;
        PmsPortalProductDetail cached = redisService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        PmsPortalProductDetail result = new PmsPortalProductDetail();
        //获取商品信息
        // ✅ 改造：selectByPrimaryKey → selectById
        PmsProduct product = productMapper.selectById(id);
        result.setProduct(product);
        //获取品牌信息
        // ✅ 改造：selectByPrimaryKey → selectById
        PmsBrand brand = brandMapper.selectById(product.getBrandId());
        result.setBrand(brand);
        //获取商品属性信息
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProductAttribute>())
        List<PmsProductAttribute> productAttributeList = productAttributeMapper.selectList(
                new LambdaQueryWrapper<PmsProductAttribute>()
                        .eq(PmsProductAttribute::getProductAttributeCategoryId, product.getProductAttributeCategoryId()));
        result.setProductAttributeList(productAttributeList);
        //获取商品属性值信息
        if(CollUtil.isNotEmpty(productAttributeList)){
            List<Long> attributeIds = productAttributeList.stream().map(PmsProductAttribute::getId).collect(Collectors.toList());
            // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProductAttributeValue>())
            List<PmsProductAttributeValue> productAttributeValueList = productAttributeValueMapper.selectList(
                    new LambdaQueryWrapper<PmsProductAttributeValue>()
                            .eq(PmsProductAttributeValue::getProductId, product.getId())
                            .in(PmsProductAttributeValue::getProductAttributeId, attributeIds));
            result.setProductAttributeValueList(productAttributeValueList);
        }
        //获取商品SKU库存信息
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsSkuStock>())
        List<PmsSkuStock> skuStockList = skuStockMapper.selectList(
                new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getProductId, product.getId()));
        result.setSkuStockList(skuStockList);
        //商品阶梯价格设置
        if(product.getPromotionType()==3){
            // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProductLadder>())
            List<PmsProductLadder> productLadderList = productLadderMapper.selectList(
                    new LambdaQueryWrapper<PmsProductLadder>().eq(PmsProductLadder::getProductId, product.getId()));
            result.setProductLadderList(productLadderList);
        }
        //商品满减价格设置
        if(product.getPromotionType()==4){
            // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProductFullReduction>())
            List<PmsProductFullReduction> productFullReductionList = productFullReductionMapper.selectList(
                    new LambdaQueryWrapper<PmsProductFullReduction>().eq(PmsProductFullReduction::getProductId, product.getId()));
            result.setProductFullReductionList(productFullReductionList);
        }
        //商品可用优惠券
        result.setCouponList(portalProductDao.getAvailableCouponList(product.getId(),product.getProductCategoryId()));
        
        // 存入缓存，30分钟过期
        redisService.set(cacheKey, result, 1800);
        return result;
    }


    /**
     * 初始对象转化为节点对象
     */
    private PmsProductCategoryNode covert(PmsProductCategory item, List<PmsProductCategory> allList) {
        PmsProductCategoryNode node = new PmsProductCategoryNode();
        BeanUtils.copyProperties(item, node);
        List<PmsProductCategoryNode> children = allList.stream()
                .filter(subItem -> subItem.getParentId().equals(item.getId()))
                .map(subItem -> covert(subItem, allList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }
}
