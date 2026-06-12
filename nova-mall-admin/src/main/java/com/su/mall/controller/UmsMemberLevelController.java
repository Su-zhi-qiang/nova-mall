package com.su.mall.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.model.UmsMemberLevel;
import com.su.mall.service.UmsMemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 会员等级管理Controller
 * @author Su
 */
@RestController
@Tag(name = "UmsMemberLevelController", description = "会员等级管理")
@RequestMapping("/memberLevel")
@RequiredArgsConstructor
public class UmsMemberLevelController {
    private final UmsMemberLevelService memberLevelService;

    @Operation(summary = "查询所有会员等级")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<UmsMemberLevel>> list(@RequestParam("defaultStatus") Integer defaultStatus) {
        List<UmsMemberLevel> memberLevelList = memberLevelService.list(defaultStatus);
        return CommonResult.success(memberLevelList);
    }
}
