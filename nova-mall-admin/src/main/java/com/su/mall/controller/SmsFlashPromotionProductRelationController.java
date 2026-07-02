package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.SmsFlashPromotionProduct;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.service.SmsFlashPromotionProductRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 限时购和商品关系管理Controller
 * 
 */
@RestController
@Tag(name = "SmsFlashPromotionProductRelationController", description = "限时购和商品关系管理")
@RequestMapping("/flashProductRelation")
@RequiredArgsConstructor
public class SmsFlashPromotionProductRelationController {
    private final SmsFlashPromotionProductRelationService relationService;

    @Operation(summary = "批量选择商品添加关联")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody List<SmsFlashPromotionProductRelation> relationList) {
        int count = relationService.create(relationList);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改关联信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody SmsFlashPromotionProductRelation relation) {
        int count = relationService.update(id, relation);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除关联")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = relationService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取关联商品促销信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<SmsFlashPromotionProductRelation> getItem(@PathVariable Long id) {
        SmsFlashPromotionProductRelation relation = relationService.getItem(id);
        return CommonResult.success(relation);
    }

    @Operation(summary = "分页查询不同场次关联及商品信息（含商品名称、货号、价格、库存）")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SmsFlashPromotionProduct>> list(@RequestParam(value = "flashPromotionId", required = false) Long flashPromotionId,
                                                                   @RequestParam(value = "flashPromotionSessionId", required = false) Long flashPromotionSessionId,
                                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<SmsFlashPromotionProduct> relationPage = relationService.list(flashPromotionId, flashPromotionSessionId, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(relationPage));
    }
}
