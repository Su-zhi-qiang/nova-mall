package com.su.mall.portal.domain;

import com.su.mall.model.CmsSubject;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;
import com.su.mall.model.SmsHomeAdvertise;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 首页内容聚合响应
 * <p>包含首页所有展示模块的数据：轮播广告、推荐品牌、秒杀活动、新品/人气推荐、专题
 * <p>由 {@link com.su.mall.portal.service.HomeService#content()} 构建返回
 */
@Getter
@Setter
public class HomeContentResult {

    @Schema(title = "轮播广告列表")
    private List<SmsHomeAdvertise> advertiseList;

    @Schema(title = "推荐品牌列表")
    private List<PmsBrand> brandList;

    @Schema(title = "当前秒杀场次信息（含商品列表）")
    private HomeFlashPromotion homeFlashPromotion;

    @Schema(title = "新品推荐商品列表")
    private List<PmsProduct> newProductList;

    @Schema(title = "人气推荐商品列表")
    private List<PmsProduct> hotProductList;

    @Schema(title = "推荐专题列表")
    private List<CmsSubject> subjectList;
}
