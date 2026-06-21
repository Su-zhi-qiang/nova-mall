package com.su.mall.portal.controller;

import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.portal.domain.OmsOrderReturnApplyParam;
import com.su.mall.portal.service.OmsPortalOrderReturnApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 退货申请管理Controller
 * @author Su
 */
@RestController
@Tag(name = "OmsPortalOrderReturnApplyController",description = "退货申请管理")
@RequestMapping("/returnApply")
@RequiredArgsConstructor
public class OmsPortalOrderReturnApplyController {
    private final OmsPortalOrderReturnApplyService returnApplyService;

    @Operation(summary = "申请退货")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody OmsOrderReturnApplyParam returnApply) {
        int count = returnApplyService.create(returnApply);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取当前用户的退货申请列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<OmsOrderReturnApply>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        CommonPage<OmsOrderReturnApply> page = returnApplyService.list(pageNum, pageSize);
        return CommonResult.success(page);
    }

    @Operation(summary = "查询订单是否有退货申请")
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public CommonResult<Map<Long, Integer>> getReturnStatus() {
        Map<Long, Integer> statusMap = returnApplyService.getReturnStatusByOrderIds(null);
        return CommonResult.success(statusMap);
    }

    @Operation(summary = "删除退货申请（软删除）")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@RequestParam("id") Long id) {
        int count = returnApplyService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
