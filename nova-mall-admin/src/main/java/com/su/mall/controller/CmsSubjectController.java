package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.CmsSubject;
import com.su.mall.service.CmsSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品专题管理Controller
 * @author Su
 */
@RestController
@Tag(name = "CmsSubjectController", description = "商品专题管理")
@RequestMapping("/subject")
@RequiredArgsConstructor
public class CmsSubjectController {
    private final CmsSubjectService subjectService;

    @Operation(summary = "获取全部商品专题")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public CommonResult<List<CmsSubject>> listAll() {
        List<CmsSubject> subjectList = subjectService.listAll();
        return CommonResult.success(subjectList);
    }

    @Operation(summary = "根据专题名称分页获取商品专题")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CmsSubject>> getList(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                        @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<CmsSubject> subjectPage = subjectService.list(keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(subjectPage));
    }
}
