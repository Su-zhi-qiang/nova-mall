package com.su.mall.portal.domain;

import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 订单详情（含商品列表）
 * <p>继承订单基础信息，扩展订单商品明细列表
 * <p>由 {@link com.su.mall.portal.dao.PortalOrderDao#getDetail} 查询构建
 *
 * @see com.su.mall.portal.service.OmsPortalOrderService#detail(Long)
 */
@Getter
@Setter
public class OmsOrderDetail extends OmsOrder {

    @Schema(title = "订单商品列表（含数量、价格、优惠分摊等）")
    private List<OmsOrderItem> orderItemList;
}
