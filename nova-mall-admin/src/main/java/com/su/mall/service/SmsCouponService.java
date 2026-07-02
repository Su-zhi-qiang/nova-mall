package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.SmsCouponParam;
import com.su.mall.model.SmsCoupon;

import java.util.List;

/**
 * 优惠券管理Service
 * <p>提供优惠券的创建、修改、删除、分页查询等后台管理功能
 * <p>优惠券类型包括：全场通用(useType=0)、指定分类(useType=1)、指定商品(useType=2)
 *
 * @see SmsCouponServiceImpl
 */
public interface SmsCouponService {
    /**
     * 添加优惠券
     */
    int create(SmsCouponParam couponParam);

    /**
     * 根据优惠券id删除优惠券
     */
    int delete(Long id);

    /**
     * 根据优惠券id更新优惠券信息
     */
    int update(Long id, SmsCouponParam couponParam);

    /**
     * 分页获取优惠券列表
     */
    Page<SmsCoupon> list(String name, Integer type, Integer pageSize, Integer pageNum);

    /**
     * 获取优惠券详情
     * @param id 优惠券表id
     */
    SmsCouponParam getItem(Long id);
}
