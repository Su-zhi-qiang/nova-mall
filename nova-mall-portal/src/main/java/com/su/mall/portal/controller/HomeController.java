package com.su.mall.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.CmsSubject;
import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsProductCategory;
import com.su.mall.portal.domain.HomeContentResult;
import com.su.mall.portal.domain.HomeFlashPromotion;
import com.su.mall.portal.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.su.mall.mapper.SmsFlashPromotionMapper;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.mapper.SmsFlashPromotionSessionMapper;
import com.su.mall.model.SmsFlashPromotion;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import com.su.mall.model.SmsFlashPromotionSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 首页内容管理Controller
 * @author Su
 */
@RestController
@Tag(name = "HomeController", description = "首页内容管理")
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;
    @Autowired
    private SmsFlashPromotionSessionMapper sessionMapper;
    @Autowired
    private SmsFlashPromotionMapper promotionMapper;
    @Autowired
    private SmsFlashPromotionProductRelationMapper relationMapper;

    @Operation(summary = "调试:秒杀场次状态")
    @RequestMapping(value = "/debug/flashStatus", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> debugFlashStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("serverTime", new Date());
        
        // 查所有启用场次
        List<SmsFlashPromotionSession> sessions = sessionMapper.selectList(
            new LambdaQueryWrapper<SmsFlashPromotionSession>()
                .eq(SmsFlashPromotionSession::getStatus, 1)
                .orderByAsc(SmsFlashPromotionSession::getStartTime));
        result.put("sessions", sessions);
        
        // 查有效活动
        Date now = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(now);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        
        List<SmsFlashPromotion> activities = promotionMapper.selectList(
            new LambdaQueryWrapper<SmsFlashPromotion>()
                .eq(SmsFlashPromotion::getStatus, 1)
                .le(SmsFlashPromotion::getStartDate, today)
                .ge(SmsFlashPromotion::getEndDate, today));
        result.put("activities", activities);
        
        // 查活动1场次8的商品
        List<SmsFlashPromotionProductRelation> products = relationMapper.selectList(
            new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, 1)
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, 8));
        result.put("session8Products", products);
        
        return CommonResult.success(result);
    }

    @Operation(summary = "首页内容信息展示")
    @RequestMapping(value = "/content", method = RequestMethod.GET)
    public CommonResult<HomeContentResult> content() {
        HomeContentResult contentResult = homeService.content();
        return CommonResult.success(contentResult);
    }

    @Operation(summary = "获取秒杀活动信息")
    @RequestMapping(value = "/flashPromotion", method = RequestMethod.GET)
    public CommonResult<HomeFlashPromotion> flashPromotion() {
        HomeFlashPromotion flashPromotion = homeService.getFlashPromotion();
        return CommonResult.success(flashPromotion);
    }

    @Operation(summary = "分页获取推荐商品")
    @RequestMapping(value = "/recommendProductList", method = RequestMethod.GET)
    public CommonResult<CommonPage<PmsProduct>> recommendProductList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<PmsProduct> page = homeService.recommendProductList(pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "获取首页商品分类")
    @RequestMapping(value = "/productCateList/{parentId}", method = RequestMethod.GET)
    public CommonResult<List<PmsProductCategory>> getProductCateList(@PathVariable Long parentId) {
        List<PmsProductCategory> productCategoryList = homeService.getProductCateList(parentId);
        return CommonResult.success(productCategoryList);
    }

    @Operation(summary = "根据分类获取专题")
    @RequestMapping(value = "/subjectList", method = RequestMethod.GET)
    public CommonResult<CommonPage<CmsSubject>> getSubjectList(@RequestParam(required = false) Long cateId,
                                                              @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<CmsSubject> page = homeService.getSubjectList(cateId, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "分页获取人气推荐商品")
    @RequestMapping(value = "/hotProductList", method = RequestMethod.GET)
    public CommonResult<CommonPage<PmsProduct>> hotProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                                @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        Page<PmsProduct> page = homeService.hotProductList(pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @Operation(summary = "分页获取新品推荐商品")
    @RequestMapping(value = "/newProductList", method = RequestMethod.GET)
    public CommonResult<CommonPage<PmsProduct>> newProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                                @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        Page<PmsProduct> page = homeService.newProductList(pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }
}