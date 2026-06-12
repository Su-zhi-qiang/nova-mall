package com.su.mall.controller;

import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.OmsOrderReturnReason;
import com.su.mall.service.OmsOrderReturnReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 退货原因管理Controller
 * @author Su
 */
@RestController
@Tag(name = "OmsOrderReturnReasonController", description = "退货原因管理")
@RequestMapping("/returnReason")
@RequiredArgsConstructor
public class OmsOrderReturnReasonController {
    private final OmsOrderReturnReasonService orderReturnReasonService;

    @Operation(summary = "添加退货原因")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody OmsOrderReturnReason returnReason) {
        int count = orderReturnReasonService.create(returnReason);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改退货原因")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody OmsOrderReturnReason returnReason) {
        int count = orderReturnReasonService.update(id, returnReason);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量删除退货原因")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@RequestParam("ids") List<Long> ids) {
        int count = orderReturnReasonService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "分页查询退货原因")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<OmsOrderReturnReason>> list(@RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<OmsOrderReturnReason> reasonPage = orderReturnReasonService.list(pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(reasonPage));
    }

    @Operation(summary = "获取单个退货原因详情信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<OmsOrderReturnReason> getItem(@PathVariable Long id) {
        OmsOrderReturnReason reason = orderReturnReasonService.getItem(id);
        return CommonResult.success(reason);
    }

    @Operation(summary = "修改退货原因启用状态")
    @RequestMapping(value = "/update/status", method = RequestMethod.POST)
    public CommonResult<Integer> updateStatus(@RequestParam(value = "status") Integer status,
                                     @RequestParam("ids") List<Long> ids) {
        int count = orderReturnReasonService.updateStatus(ids, status);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
