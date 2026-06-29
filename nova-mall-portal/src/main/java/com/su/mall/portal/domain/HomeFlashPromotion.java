package com.su.mall.portal.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 首页秒杀场次信息
 * <p>包含当前场次和下一场次的时间范围，以及当前场次的商品列表
 * <p>由 {@link com.su.mall.portal.service.HomeService#getFlashPromotion()} 构建返回
 */
@Getter
@Setter
public class HomeFlashPromotion {

    @Schema(title = "当前场次开始时间")
    private Date startTime;

    @Schema(title = "当前场次结束时间")
    private Date endTime;

    @Schema(title = "下一场次开始时间")
    private Date nextStartTime;

    @Schema(title = "下一场次结束时间")
    private Date nextEndTime;

    @Schema(title = "当前场次的秒杀商品列表")
    private List<FlashPromotionProduct> productList;
}
