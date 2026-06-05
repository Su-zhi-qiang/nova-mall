package com.su.mall.controller;


import com.su.mall.common.api.CommonResult;
import com.su.mall.service.PmsProductVerifyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/product/verify")
@Tag(name = "PmsProductVerifyController", description = "商品审核管理")
@RequiredArgsConstructor
public class PmsProductVerifyController {
    private final PmsProductVerifyRecordService productVerifyService;


    @Operation(summary = "获取商品审核详情")
    @GetMapping("/verifyInfo/{id}")
    public CommonResult<Map<String,Object>> getVerifyInfo(@PathVariable Long id){
        Map<String,Object> result = productVerifyService.getVerifyInfo(id);
        return CommonResult.success(result);
    }

    @Operation(summary = "更新商品审核状态")
    @PostMapping("/update/verifyStatus")
    public  CommonResult<Void> updateVerifyStatus(
            @RequestParam Long productId,
            @RequestParam Integer verifyStatus,
            @RequestParam(required = false) String verifyRemark
    ){
        productVerifyService.updateVerifyStatus(productId, verifyStatus, verifyRemark);
        return CommonResult.success(null);
    }


}
