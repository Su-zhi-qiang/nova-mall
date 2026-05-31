package com.su.mall.portal.domain;

import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 包含商品信息的订单详情
 * @author Su
 */
@Getter
@Setter
public class OmsOrderDetail extends OmsOrder {
    @Schema(title = "订单商品列表")
    private List<OmsOrderItem> orderItemList;
}
