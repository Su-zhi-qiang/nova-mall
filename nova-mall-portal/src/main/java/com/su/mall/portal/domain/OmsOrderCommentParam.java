package com.su.mall.portal.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品评价参数
 */
@Data
public class OmsOrderCommentParam {
    @Schema(title = "订单项ID")
    private Long orderItemId;

    @Schema(title = "评分(1-5)")
    private Integer star;

    @Schema(title = "评价内容")
    private String content;

    @Schema(title = "图片URL，逗号分隔")
    private String pics;
}
