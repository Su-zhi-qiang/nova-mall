package com.su.mall.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 订单商品评价
 */
@Data
@TableName("oms_order_comment")
public class OmsOrderComment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(title = "订单ID")
    private Long orderId;

    @Schema(title = "订单项ID")
    private Long orderItemId;

    @Schema(title = "商品ID")
    private Long productId;

    @Schema(title = "会员ID")
    private Long memberId;

    @Schema(title = "会员昵称")
    private String memberNickName;

    @Schema(title = "评分(1-5)")
    private Integer star;

    @Schema(title = "评价内容")
    private String content;

    @Schema(title = "图片URL，逗号分隔")
    private String pics;

    @Schema(title = "显示状态(0隐藏/1显示)")
    private Integer showStatus;

    @Schema(title = "创建时间")
    private Date createTime;
}
