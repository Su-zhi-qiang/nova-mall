package com.su.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.PmsProductOperateLog;
import com.su.mall.service.PmsProductOperateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

/**
 * 商品操作日志Controller
 */
@RestController
@Tag(name = "PmsProductOperateLogController", description = "商品操作日志管理")
@RequestMapping("/product")
@RequiredArgsConstructor
public class PmsProductOperateLogController {

    private final PmsProductOperateLogService productOperateLogService;

    @Operation(summary = "根据商品ID查询操作日志")
    @RequestMapping(value = "/operateLog/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<PmsProductOperateLog>> list(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<PmsProductOperateLog> logPage = productOperateLogService.listByProductId(productId, pageNum, pageSize);
        // 将IPage转换为Page类型
        Page<PmsProductOperateLog> page = (Page<PmsProductOperateLog>) logPage;
        return CommonResult.success(CommonPage.restPage(page));
    }
}