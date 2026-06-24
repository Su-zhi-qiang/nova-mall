package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.mapper.OmsOrderMapper;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * 更新订单支付状态
 */
@RequiredArgsConstructor
public class UpdatePaymentStatusHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder fullOrder = context.getAttribute("fullOrder");
        Integer payType = context.getAttribute("payType");
        fullOrder.setStatus(1);
        fullOrder.setPaymentTime(new Date());
        fullOrder.setPayType(payType);
        orderMapper.updateById(fullOrder);
        handleNext(context);
    }
}