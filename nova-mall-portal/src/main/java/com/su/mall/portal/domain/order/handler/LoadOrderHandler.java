package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.mapper.OmsOrderMapper;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * 加载并校验订单
 */
@RequiredArgsConstructor
public class LoadOrderHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        Long orderId = context.getAttribute("orderId");
        OmsOrder fullOrder = orderMapper.selectById(orderId);
        if (fullOrder == null) {
            Asserts.fail("订单不存在");
        }
        if (fullOrder.getStatus() != 0) {
            Asserts.fail("订单状态异常，无法支付");
        }
        context.putAttribute("fullOrder", fullOrder);
        handleNext(context);
    }
}