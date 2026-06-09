package com.su.mall.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;
import com.su.mall.portal.service.PmsPortalBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页品牌推荐管理Controller
 * @author Su
 */
@Controller
@Tag(name = "PmsPortalBrandController", description = "前台品牌管理")
@RequestMapping("/brand")
@RequiredArgsConstructor
public class PmsPortalBrandController {

    private final PmsPortalBrandService portalBrandService;

    @Operation(summary = "分页获取推荐品牌")
    @RequestMapping(value = "/recommendList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<PmsBrand>> recommendList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        Page<PmsBrand> brandPage = portalBrandService.list(pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(brandPage));
    }

    @Operation(summary = "获取品牌详情")
    @RequestMapping(value = "/detail/{brandId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<PmsBrand> detail(@PathVariable Long brandId) {
        PmsBrand brand = portalBrandService.detail(brandId);
        return CommonResult.success(brand);
    }

    @Operation(summary = "分页获取品牌相关商品")
    @RequestMapping(value = "/productList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<PmsProduct>> productList(@RequestParam Long brandId,
                                                            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        Page<PmsProduct> page = portalBrandService.productList(brandId, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }
}