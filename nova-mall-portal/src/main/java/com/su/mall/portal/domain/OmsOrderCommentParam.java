package com.su.mall.portal.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品评价请求参数
 * <p>由前端提交，包含评价的订单项、评分、内容和图片
 *
 * @see com.su.mall.portal.service.OmsOrderCommentService#create
 */
@Data
public class OmsOrderCommentParam {

    @Schema(title = "订单商品项ID")
    private Long orderItemId;

    @Schema(title = "评分（1-5星）")
    private Integer star;

    @Schema(title = "评价文字内容")
    private String content;

    @Schema(title = "评价图片URL（多张以逗号分隔）")
    private String pics;
}
