package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.exception.Asserts;
import com.su.mall.dao.SmsCouponDao;
import com.su.mall.dao.SmsCouponProductCategoryRelationDao;
import com.su.mall.dao.SmsCouponProductRelationDao;
import com.su.mall.dto.SmsCouponParam;
import com.su.mall.mapper.SmsCouponMapper;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.mapper.SmsCouponProductCategoryRelationMapper;
import com.su.mall.mapper.SmsCouponProductRelationMapper;
import com.su.mall.model.*;
import com.su.mall.service.SmsCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 优惠券管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class SmsCouponServiceImpl implements SmsCouponService {
    private final SmsCouponMapper couponMapper;
    private final SmsCouponProductRelationMapper productRelationMapper;
    private final SmsCouponProductCategoryRelationMapper productCategoryRelationMapper;
    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponProductRelationDao productRelationDao;
    private final SmsCouponProductCategoryRelationDao productCategoryRelationDao;
    private final SmsCouponDao couponDao;
    @Override
    @Transactional
    public int create(SmsCouponParam couponParam) {
        couponParam.setCount(couponParam.getPublishCount());
        couponParam.setUseCount(0);
        couponParam.setReceiveCount(0);
        //插入优惠券表
        int count = couponMapper.insert(couponParam);
        //插入优惠券和商品关系表
        if(Integer.valueOf(2).equals(couponParam.getUseType()) && couponParam.getProductRelationList() != null){
            for(SmsCouponProductRelation productRelation:couponParam.getProductRelationList()){
                productRelation.setCouponId(couponParam.getId());
            }
            productRelationDao.insertList(couponParam.getProductRelationList());
        }
        //插入优惠券和商品分类关系表
        if(Integer.valueOf(1).equals(couponParam.getUseType()) && couponParam.getProductCategoryRelationList() != null){
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            productCategoryRelationDao.insertList(couponParam.getProductCategoryRelationList());
        }
        return count;
    }

    @Override
    @Transactional
    public int delete(Long id) {
        long unUsedCount = couponHistoryMapper.selectCount(
                new LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getCouponId, id)
                        .eq(SmsCouponHistory::getUseStatus, 0));
        if (unUsedCount > 0) {
            Asserts.fail("该优惠券有未使用的领取记录，不能删除");
        }
        int count = couponMapper.deleteById(id);
        //删除商品关联
        deleteProductRelation(id);
        //删除商品分类关联
        deleteProductCategoryRelation(id);
        return count;
    }

    private void deleteProductCategoryRelation(Long id) {
        productCategoryRelationMapper.delete(new LambdaQueryWrapper<SmsCouponProductCategoryRelation>()
                .eq(SmsCouponProductCategoryRelation::getCouponId, id));
    }

    private void deleteProductRelation(Long id) {
        productRelationMapper.delete(new LambdaQueryWrapper<SmsCouponProductRelation>()
                .eq(SmsCouponProductRelation::getCouponId, id));
    }

    @Override
    @Transactional
    public int update(Long id, SmsCouponParam couponParam) {
        couponParam.setId(id);
        int count = couponMapper.updateById(couponParam);
        // 先删除所有旧的关联关系（无论useType是否变化）
        deleteProductRelation(id);
        deleteProductCategoryRelation(id);
        // 根据新的useType重新插入关联关系
        if (Integer.valueOf(2).equals(couponParam.getUseType()) && couponParam.getProductRelationList() != null) {
            for (SmsCouponProductRelation productRelation : couponParam.getProductRelationList()) {
                productRelation.setCouponId(couponParam.getId());
            }
            productRelationDao.insertList(couponParam.getProductRelationList());
        }
        if (Integer.valueOf(1).equals(couponParam.getUseType()) && couponParam.getProductCategoryRelationList() != null) {
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            productCategoryRelationDao.insertList(couponParam.getProductCategoryRelationList());
        }
        return count;
    }

    @Override
    public Page<SmsCoupon> list(String name, Integer type, Integer pageSize, Integer pageNum) {
        Page<SmsCoupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<>();
        if(!StrUtil.isEmpty(name)){
            wrapper.like(SmsCoupon::getName, name);
        }
        if(type!=null){
            wrapper.eq(SmsCoupon::getType, type);
        }
        return couponMapper.selectPage(page, wrapper);
    }

    @Override
    public SmsCouponParam getItem(Long id) {
        return couponDao.getItem(id);
    }
}
