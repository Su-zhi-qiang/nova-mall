package com.su.mall.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.model.OmsOrderSetting;
import com.su.mall.service.OmsOrderSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

/**
 * 订单设置管理Controller
 * @author Su
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "OmsOrderSettingController", description = "订单设置管理")
@RequestMapping("/orderSetting")
public class OmsOrderSettingController {
    private final OmsOrderSettingService orderSettingService;

    @Operation(summary = "获取指定订单设置")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<OmsOrderSetting> getItem(@PathVariable Long id) {
        OmsOrderSetting orderSetting = orderSettingService.getItem(id);
        return CommonResult.success(orderSetting);
    }

    @Operation(summary = "修改指定订单设置")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody OmsOrderSetting orderSetting) {
        int count = orderSettingService.update(id,orderSetting);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
