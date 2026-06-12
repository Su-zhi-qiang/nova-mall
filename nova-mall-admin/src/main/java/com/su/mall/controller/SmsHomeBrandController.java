package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.SmsHomeBrand;
import com.su.mall.service.SmsHomeBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页品牌管理Controller
 * @author Su
 */
@RestController
@Tag(name = "SmsHomeBrandController", description = "首页品牌管理")
@RequestMapping("/home/brand")
@RequiredArgsConstructor
public class SmsHomeBrandController {
    private final SmsHomeBrandService homeBrandService;

    @Operation(summary = "添加首页推荐品牌")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody List<SmsHomeBrand> homeBrandList) {
        int count = homeBrandService.create(homeBrandList);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改推荐品牌排序")
    @RequestMapping(value = "/update/sort/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> updateSort(@PathVariable Long id, Integer sort) {
        int count = homeBrandService.updateSort(id, sort);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量删除推荐品牌")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@RequestParam("ids") List<Long> ids) {
        int count = homeBrandService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量修改推荐品牌状态")
    @RequestMapping(value = "/update/recommendStatus", method = RequestMethod.POST)
    public CommonResult<Integer> updateRecommendStatus(@RequestParam("ids") List<Long> ids, @RequestParam Integer recommendStatus) {
        int count = homeBrandService.updateRecommendStatus(ids, recommendStatus);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "分页查询推荐品牌")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SmsHomeBrand>> list(@RequestParam(value = "brandName", required = false) String brandName,
                                                       @RequestParam(value = "recommendStatus", required = false) Integer recommendStatus,
                                                       @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<SmsHomeBrand> homeBrandPage = homeBrandService.list(brandName, recommendStatus, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(homeBrandPage));
    }
}
