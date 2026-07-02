package com.su.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.service.RedisService;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.portal.dao.PortalProductDao;
import com.su.mall.portal.domain.PmsPortalProductDetail;
import com.su.mall.portal.domain.PmsProductCategoryNode;
import com.su.mall.portal.service.PmsPortalProductService;
import com.su.mall.portal.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台订单管理Service实现类
 * 
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
    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SmsFlashPromotionSessionMapper promotionSessionMapper;
    private final SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;

    @Override
    public Page<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort) {
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<PmsProduct>()
                .eq(PmsProduct::getDeleteStatus, 0)
                .eq(PmsProduct::getPublishStatus, 1)
                .like(StrUtil.isNotEmpty(keyword), PmsProduct::getName, keyword)
                .eq(brandId != null, PmsProduct::getBrandId, brandId)
                .eq(productCategoryId != null, PmsProduct::getProductCategoryId, productCategoryId);
        
        // 修复：不要传 null 给 orderByXxx 方法
        if (sort != null) {
            switch (sort) {
                case 1:
                    wrapper.orderByDesc(PmsProduct::getId);
                    break;
                case 2:
                    wrapper.orderByDesc(PmsProduct::getSale);
                    break;
                case 3:
                    wrapper.orderByAsc(PmsProduct::getPrice);
                    break;
                case 4:
                    wrapper.orderByDesc(PmsProduct::getPrice);
                    break;
            }
        }
        
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public List<PmsProductCategoryNode> categoryTreeList() {
        List<PmsProductCategory> allList = productCategoryMapper.selectList(new LambdaQueryWrapper<PmsProductCategory>());
        return allList.stream()
                .filter(item -> item.getParentId().equals(0L))
                .map(item -> covert(item, allList))
                .collect(Collectors.toList());
    }

    @Override
    public PmsPortalProductDetail detail(Long id) {
        String cacheKey = "product:detail:" + id;
        
        // 更新浏览量（即使缓存命中也要更新）
        LambdaUpdateWrapper<PmsProduct> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PmsProduct::getId, id);
        updateWrapper.setSql("view_count = IFNULL(view_count, 0) + 1");
        productMapper.update(null, updateWrapper);
        
        // 清除缓存，确保下次获取最新浏览量数据
        redisService.del(cacheKey);
        
        PmsPortalProductDetail cached = redisService.get(cacheKey);
        if (cached != null) {
            // 更新缓存数据中的浏览量
            if (cached.getProduct() != null) {
                PmsProduct product = cached.getProduct();
                if (product.getViewCount() == null) {
                    product.setViewCount(1);
                } else {
                    product.setViewCount(product.getViewCount() + 1);
                }
            }
            return cached;
        }

        PmsPortalProductDetail result = new PmsPortalProductDetail();
        //获取商品信息
        PmsProduct product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        result.setProduct(product);
        //获取品牌信息
        PmsBrand brand = brandMapper.selectById(product.getBrandId());
        result.setBrand(brand);
        //获取商品属性信息
        List<PmsProductAttribute> productAttributeList = productAttributeMapper.selectList(
                new LambdaQueryWrapper<PmsProductAttribute>()
                        .eq(PmsProductAttribute::getProductAttributeCategoryId, product.getProductAttributeCategoryId()));
        result.setProductAttributeList(productAttributeList);
        //获取商品属性值信息
        if(CollUtil.isNotEmpty(productAttributeList)){
            List<Long> attributeIds = productAttributeList.stream().map(PmsProductAttribute::getId).collect(Collectors.toList());
            List<PmsProductAttributeValue> productAttributeValueList = productAttributeValueMapper.selectList(
                    new LambdaQueryWrapper<PmsProductAttributeValue>()
                            .eq(PmsProductAttributeValue::getProductId, product.getId())
                            .in(PmsProductAttributeValue::getProductAttributeId, attributeIds));
            result.setProductAttributeValueList(productAttributeValueList);
        }
        //获取商品SKU库存信息
        List<PmsSkuStock> skuStockList = skuStockMapper.selectList(
                new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getProductId, product.getId()));
        result.setSkuStockList(skuStockList);
        //商品阶梯价格设置
        if(product.getPromotionType() != null && product.getPromotionType() == 3){
            List<PmsProductLadder> productLadderList = productLadderMapper.selectList(
                    new LambdaQueryWrapper<PmsProductLadder>().eq(PmsProductLadder::getProductId, product.getId()));
            result.setProductLadderList(productLadderList);
        }
        //商品满减价格设置
        if(product.getPromotionType() != null && product.getPromotionType() == 4){
            List<PmsProductFullReduction> productFullReductionList = productFullReductionMapper.selectList(
                    new LambdaQueryWrapper<PmsProductFullReduction>().eq(PmsProductFullReduction::getProductId, product.getId()));
            result.setProductFullReductionList(productFullReductionList);
        }
        //商品可用优惠券
        result.setCouponList(portalProductDao.getAvailableCouponList(product.getId(),product.getProductCategoryId()));

        // ========== 查询秒杀活动信息 ==========
        fillFlashPromotionInfo(result, id);

        // 存入缓存，30分钟过期
        redisService.set(cacheKey, result, 1800);
        return result;
    }

    /**
     * 填充秒杀活动信息
     */
    private void fillFlashPromotionInfo(PmsPortalProductDetail result, Long productId) {
        Date now = new Date();
        Date currDate = DateUtil.getDatePart(now);
        Date currTime = DateUtil.getTimePart(now);

        // 查询当前有效的秒杀活动
        List<SmsFlashPromotion> activePromotions = flashPromotionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotion>()
                        .eq(SmsFlashPromotion::getStatus, 1)
                        .le(SmsFlashPromotion::getStartDate, currDate)
                        .ge(SmsFlashPromotion::getEndDate, currDate));

        if (CollUtil.isEmpty(activePromotions)) {
            return;
        }

        // 查询当前有效的场次
        List<SmsFlashPromotionSession> activeSessions = promotionSessionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionSession>()
                        .eq(SmsFlashPromotionSession::getStatus, 1)
                        .le(SmsFlashPromotionSession::getStartTime, currTime)
                        .ge(SmsFlashPromotionSession::getEndTime, currTime));

        if (CollUtil.isEmpty(activeSessions)) {
            return;
        }

        // 查询该商品在当前场次的秒杀关联（按sort排序，取第一个）
        List<SmsFlashPromotionProductRelation> flashRelations = flashPromotionProductRelationMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                        .eq(SmsFlashPromotionProductRelation::getProductId, productId)
                        .in(SmsFlashPromotionProductRelation::getFlashPromotionId,
                                activePromotions.stream().map(SmsFlashPromotion::getId).collect(Collectors.toList()))
                        .in(SmsFlashPromotionProductRelation::getFlashPromotionSessionId,
                                activeSessions.stream().map(SmsFlashPromotionSession::getId).collect(Collectors.toList()))
                        .isNotNull(SmsFlashPromotionProductRelation::getFlashPromotionPrice)
                        .orderByAsc(SmsFlashPromotionProductRelation::getSort));

        if (CollUtil.isEmpty(flashRelations)) {
            return;
        }

        // 取第一个匹配的结果，从每日快照表读取库存
        SmsFlashPromotionProductRelation flashRelation = flashRelations.get(0);
        SmsFlashPromotionDailyStock dailyStock = dailyStockMapper.getDailyStock(flashRelation.getId());
        if (dailyStock == null || dailyStock.getStock() == null || dailyStock.getStock() <= 0) {
            return;
        }

        // 设置秒杀信息
        result.setFlashPromotion(true);
        result.setFlashPromotionRelationId(flashRelation.getId());
        result.setFlashPromotionPrice(flashRelation.getFlashPromotionPrice());
        result.setFlashPromotionCount(dailyStock.getStock());
        result.setFlashPromotionSold(dailyStock.getSold() != null ? dailyStock.getSold() : 0);
        result.setFlashPromotionLimit(flashRelation.getFlashPromotionLimit());

        // 查找对应的场次信息，填充场次时间
        for (SmsFlashPromotionSession session : activeSessions) {
            if (session.getId().equals(flashRelation.getFlashPromotionSessionId())) {
                result.setFlashSessionStartTime(DateUtil.mergeDateAndTime(0, session.getStartTime()));
                result.setFlashSessionEndTime(DateUtil.mergeDateAndTime(0, session.getEndTime()));
                break;
            }
        }
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
