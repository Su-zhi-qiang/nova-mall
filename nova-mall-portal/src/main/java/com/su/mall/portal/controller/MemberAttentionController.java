package com.su.mall.portal.controller;

import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.portal.domain.MemberBrandAttention;
import com.su.mall.portal.service.MemberAttentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

/**
 * 会员关注品牌管理Controller
 * @author Su
 */
@RestController
@Tag(name = "MemberAttentionController",description = "会员关注品牌管理")
@RequestMapping("/member/attention")
@RequiredArgsConstructor
public class MemberAttentionController {
    private final MemberAttentionService memberAttentionService;
    @Operation(summary = "添加品牌关注")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<Integer> add(@RequestBody MemberBrandAttention memberBrandAttention) {
        int count = memberAttentionService.add(memberBrandAttention);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @Operation(summary = "取消品牌关注")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(Long brandId) {
        int count = memberAttentionService.delete(brandId);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @Operation(summary = "分页查询当前用户品牌关注列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MemberBrandAttention>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<MemberBrandAttention> page = memberAttentionService.list(pageNum,pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "根据品牌ID获取品牌关注详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public CommonResult<MemberBrandAttention> detail(@RequestParam Long brandId) {
        MemberBrandAttention memberBrandAttention = memberAttentionService.detail(brandId);
        return CommonResult.success(memberBrandAttention);
    }

    @Operation(summary = "清空当前用户品牌关注列表")
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public CommonResult<Void> clear() {
        memberAttentionService.clear();
        return CommonResult.success(null);
    }
}
