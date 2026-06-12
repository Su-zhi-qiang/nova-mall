package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.PmsProductAttributeParam;
import com.su.mall.dto.ProductAttrInfo;
import com.su.mall.model.PmsProductAttribute;
import com.su.mall.service.PmsProductAttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品属性管理Controller
 * @author Su
 */
@RestController
@Tag(name = "PmsProductAttributeController", description = "商品属性管理")
@RequestMapping("/productAttribute")
@RequiredArgsConstructor
public class PmsProductAttributeController {
    private final PmsProductAttributeService productAttributeService;

    @Operation(summary = "根据分类查询属性列表或参数列表")
    @Parameters({@Parameter(name = "type", description = "0表示属性，1表示参数", required = true,in = ParameterIn.QUERY, schema = @Schema(type = "integer"))})
    @RequestMapping(value = "/list/{cid}", method = RequestMethod.GET)
    public CommonResult<CommonPage<PmsProductAttribute>> getList(@PathVariable Long cid,
                                                                 @RequestParam(value = "type") Integer type,
                                                                 @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<PmsProductAttribute> attributePage = productAttributeService.getList(cid, type, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(attributePage));
    }

    @Operation(summary = "添加商品属性信息")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody PmsProductAttributeParam productAttributeParam) {
        int count = productAttributeService.create(productAttributeParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "修改商品属性信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody PmsProductAttributeParam productAttributeParam) {
        int count = productAttributeService.update(id, productAttributeParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "查询单个商品属性")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<PmsProductAttribute> getItem(@PathVariable Long id) {
        PmsProductAttribute productAttribute = productAttributeService.getItem(id);
        return CommonResult.success(productAttribute);
    }

    @Operation(summary = "批量删除商品属性")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@RequestParam("ids") List<Long> ids) {
        int count = productAttributeService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "根据商品分类的id获取商品属性及属性分类")
    @RequestMapping(value = "/attrInfo/{productCategoryId}", method = RequestMethod.GET)
    public CommonResult<List<ProductAttrInfo>> getAttrInfo(@PathVariable Long productCategoryId) {
        List<ProductAttrInfo> productAttrInfoList = productAttributeService.getProductAttrInfo(productCategoryId);
        return CommonResult.success(productAttrInfoList);
    }
}
