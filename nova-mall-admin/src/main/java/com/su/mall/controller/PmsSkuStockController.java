package com.su.mall.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.service.PmsSkuStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品SKU库存管理Controller
 * 
 */
@RestController
@Tag(name = "PmsSkuStockController", description = "sku商品库存管理")
@RequestMapping("/sku")
@RequiredArgsConstructor
public class PmsSkuStockController {
    private final PmsSkuStockService skuStockService;

    @Operation(summary = "根据商品ID及sku编码模糊搜索sku库存")
    @RequestMapping(value = "/{pid}", method = RequestMethod.GET)
    public CommonResult<List<PmsSkuStock>> getList(@PathVariable Long pid, @RequestParam(value = "keyword",required = false) String keyword) {
        List<PmsSkuStock> skuStockList = skuStockService.getList(pid, keyword);
        return CommonResult.success(skuStockList);
    }
    @Operation(summary = "批量更新sku库存信息")
    @RequestMapping(value ="/update/{pid}",method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long pid,@RequestBody List<PmsSkuStock> skuStockList){
        int count = skuStockService.update(pid,skuStockList);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }
}
