package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.PmsBrandParam;
import com.su.mall.model.PmsBrand;
import com.su.mall.service.PmsBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品品牌管理Controller
 * @author Su
 */
@RestController
@Tag(name = "PmsBrandController", description = "商品品牌管理")
@RequestMapping("/brand")
@RequiredArgsConstructor
public class PmsBrandController {
    private final PmsBrandService brandService;

    @Operation(summary =  "获取全部品牌列表")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public CommonResult<List<PmsBrand>> getList() {
        return CommonResult.success(brandService.listAllBrand());
    }

    @Operation(summary =  "添加品牌")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@Validated @RequestBody PmsBrandParam pmsBrand) {
        CommonResult<Integer> commonResult;
        int count = brandService.createBrand(pmsBrand);
        if (count == 1) {
            commonResult = CommonResult.success(count);
        } else {
            commonResult = CommonResult.failed();
        }
        return commonResult;
    }

    @Operation(summary =  "更新品牌")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable("id") Long id,
                               @Validated @RequestBody PmsBrandParam pmsBrandParam) {
        CommonResult<Integer> commonResult;
        int count = brandService.updateBrand(id, pmsBrandParam);
        if (count == 1) {
            commonResult = CommonResult.success(count);
        } else {
            commonResult = CommonResult.failed();
        }
        return commonResult;
    }

    @Operation(summary =  "删除品牌")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public CommonResult<Void> delete(@PathVariable("id") Long id) {
        int count = brandService.deleteBrand(id);
        if (count == 1) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary =  "根据品牌名称分页获取品牌列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<PmsBrand>> getList(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "showStatus",required = false) Integer showStatus,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<PmsBrand> brandPage = brandService.listBrand(keyword,showStatus,pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(brandPage));
    }

    @Operation(summary =  "根据编号查询品牌信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<PmsBrand> getItem(@PathVariable("id") Long id) {
        return CommonResult.success(brandService.getBrand(id));
    }

    @Operation(summary =  "批量删除品牌")
    @RequestMapping(value = "/delete/batch", method = RequestMethod.POST)
    public CommonResult<Integer> deleteBatch(@RequestParam("ids") List<Long> ids) {
        int count = brandService.deleteBrand(ids);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary =  "批量更新显示状态")
    @RequestMapping(value = "/update/showStatus", method = RequestMethod.POST)
    public CommonResult<Integer> updateShowStatus(@RequestParam("ids") List<Long> ids,
                                  @RequestParam("showStatus") Integer showStatus) {
        int count = brandService.updateShowStatus(ids, showStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary =  "批量更新厂家制造商状态")
    @RequestMapping(value = "/update/factoryStatus", method = RequestMethod.POST)
    public CommonResult<Integer> updateFactoryStatus(@RequestParam("ids") List<Long> ids,
                                     @RequestParam("factoryStatus") Integer factoryStatus) {
        int count = brandService.updateFactoryStatus(ids, factoryStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }
}
