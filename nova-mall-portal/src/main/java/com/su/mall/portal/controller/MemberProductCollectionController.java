package com.su.mall.portal.controller;

import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.portal.domain.MemberProductCollection;
import com.su.mall.portal.service.MemberCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

/**
 * 会员商品收藏管理Controller
 * 
 */
@RestController
@Tag(name = "MemberCollectionController",description = "会员收藏管理")
@RequestMapping("/member/productCollection")
@RequiredArgsConstructor
public class MemberProductCollectionController {
    private final MemberCollectionService memberCollectionService;

    @Operation(summary = "添加商品收藏")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<Integer> add(@RequestBody MemberProductCollection productCollection) {
        int count = memberCollectionService.add(productCollection);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "删除商品收藏")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(Long productId) {
        int count = memberCollectionService.delete(productId);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    @Operation(summary = "显示当前用户商品收藏列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MemberProductCollection>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<MemberProductCollection> page = memberCollectionService.list(pageNum,pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "显示商品收藏详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public CommonResult<MemberProductCollection> detail(@RequestParam Long productId) {
        MemberProductCollection memberProductCollection = memberCollectionService.detail(productId);
        return CommonResult.success(memberProductCollection);
    }

    @Operation(summary = "清空当前用户商品收藏列表")
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public CommonResult<Void> clear() {
        memberCollectionService.clear();
        return CommonResult.success(null);
    }
}
