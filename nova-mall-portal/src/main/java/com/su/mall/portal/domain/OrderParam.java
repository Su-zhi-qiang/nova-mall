package com.su.mall.portal.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 创建订单请求参数
 * <p>前端提交订单时携带的全部信息，由 {@link com.su.mall.portal.controller.OmsPortalOrderController} 接收
 *
 * @see com.su.mall.portal.service.OmsPortalOrderService#generateOrder(OrderParam)
 */
@Data
@EqualsAndHashCode
public class OrderParam {

    @Schema(title = "收货地址ID")
    private Long memberReceiveAddressId;

    @Schema(title = "使用的优惠券ID（可选）")
    private Long couponId;

    @Schema(title = "使用的积分数（可选，抵扣部分金额）")
    private Integer useIntegration;

    @Schema(title = "支付方式：1=支付宝, 2=微信")
    private Integer payType;

    @Schema(title = "选中的购物车商品ID列表")
    private List<Long> cartIds;
}
