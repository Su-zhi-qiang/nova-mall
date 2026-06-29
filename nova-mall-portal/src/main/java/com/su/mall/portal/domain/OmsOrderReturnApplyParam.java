package com.su.mall.portal.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 退货申请请求参数
 * <p>包含退货所需的全部信息：商品信息、退货人信息、退货原因和凭证图片
 *
 * @see com.su.mall.portal.service.OmsPortalOrderReturnApplyService#create
 */
@Getter
@Setter
public class OmsOrderReturnApplyParam {

    @Schema(title = "订单ID")
    private Long orderId;

    @Schema(title = "退货商品ID")
    private Long productId;

    @Schema(title = "订单编号")
    private String orderSn;

    @Schema(title = "会员用户名")
    private String memberUsername;

    @Schema(title = "退货联系人姓名")
    private String returnName;

    @Schema(title = "退货联系人电话")
    private String returnPhone;

    @Schema(title = "商品图片")
    private String productPic;

    @Schema(title = "商品名称")
    private String productName;

    @Schema(title = "商品品牌")
    private String productBrand;

    @Schema(title = "商品销售属性（如：颜色：红色；尺码：xl）")
    private String productAttr;

    @Schema(title = "退货数量")
    private Integer productCount;

    @Schema(title = "商品单价")
    private BigDecimal productPrice;

    @Schema(title = "商品实际支付单价")
    private BigDecimal productRealPrice;

    @Schema(title = "退货原因")
    private String reason;

    @Schema(title = "问题描述")
    private String description;

    @Schema(title = "凭证图片URL（多张以逗号分隔）")
    private String proofPics;
}
