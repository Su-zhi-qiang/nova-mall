package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.SmsCouponParam;
import com.su.mall.model.SmsCoupon;
import com.su.mall.service.SmsCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券管理Controller
 * 
 */
@RestController
@Tag(name = "SmsCouponController", description = "优惠券管理")
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class SmsCouponController {
    private final SmsCouponService couponService;
    @Operation(summary = "添加优惠券")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> add(@RequestBody SmsCouponParam couponParam) {
        int count = couponService.create(couponParam);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除优惠券")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = couponService.delete(id);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改优惠券")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long id,@RequestBody SmsCouponParam couponParam) {
        int count = couponService.update(id,couponParam);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "根据优惠券名称和类型分页获取优惠券列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SmsCoupon>> list(
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "type",required = false) Integer type,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<SmsCoupon> couponPage = couponService.list(name,type,pageSize,pageNum);
        return CommonResult.success(CommonPage.restPage(couponPage));
    }

    @Operation(summary = "获取单个优惠券的详细信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<SmsCouponParam> getItem(@PathVariable Long id) {
        SmsCouponParam couponParam = couponService.getItem(id);
        if(couponParam == null){
            return CommonResult.failed("优惠券不存在");
        }
        return CommonResult.success(couponParam);
    }
}
