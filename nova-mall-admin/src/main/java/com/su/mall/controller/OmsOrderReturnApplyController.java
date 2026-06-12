package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.OmsOrderReturnApplyResult;
import com.su.mall.dto.OmsReturnApplyQueryParam;
import com.su.mall.dto.OmsUpdateStatusParam;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.service.OmsOrderReturnApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单退货申请管理Controller
 * @author Su
 */
@RestController
@Tag(name = "OmsOrderReturnApplyController", description = "订单退货申请管理")
@RequestMapping("/returnApply")
@RequiredArgsConstructor
public class OmsOrderReturnApplyController {
    private final OmsOrderReturnApplyService returnApplyService;

    @Operation(summary = "分页查询退货申请")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<OmsOrderReturnApply>> list(OmsReturnApplyQueryParam queryParam,
                                                              @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<OmsOrderReturnApply> returnApplyPage = returnApplyService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(returnApplyPage));
    }

    @Operation(summary = "批量删除退货申请")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@RequestParam("ids") List<Long> ids) {
        int count = returnApplyService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取退货申请详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<OmsOrderReturnApplyResult> getItem(@PathVariable Long id) {
        OmsOrderReturnApplyResult result = returnApplyService.getItem(id);
        return CommonResult.success(result);
    }

    @Operation(summary = "修改退货申请状态")
    @RequestMapping(value = "/update/status/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestBody OmsUpdateStatusParam statusParam) {
        int count = returnApplyService.updateStatus(id, statusParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

}
