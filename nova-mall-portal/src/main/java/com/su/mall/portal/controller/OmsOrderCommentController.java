package com.su.mall.portal.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.model.OmsOrderComment;
import com.su.mall.portal.domain.OmsOrderCommentParam;
import com.su.mall.portal.service.OmsOrderCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品评价Controller
 */
@RestController
@Tag(name = "OmsOrderCommentController", description = "商品评价管理")
@RequestMapping("/comment")
@RequiredArgsConstructor
public class OmsOrderCommentController {
    private final OmsOrderCommentService commentService;

    @Operation(summary = "提交商品评价")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody OmsOrderCommentParam param) {
        int count = commentService.create(param);
        if (count > 0) {
            return CommonResult.success(count, "评价成功");
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取商品评价列表")
    @RequestMapping(value = "/list/{productId}", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> list(@PathVariable Long productId) {
        Map<String, Object> result = commentService.listByProductId(productId);
        return CommonResult.success(result);
    }
}
