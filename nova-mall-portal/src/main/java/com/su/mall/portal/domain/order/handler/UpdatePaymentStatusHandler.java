package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.mapper.OmsOrderMapper;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * 支付成功链 - 第2步：更新订单支付状态
 * <p>将订单状态从"待付款"(0)更新为"待发货"(1)，记录支付时间和支付方式
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class UpdatePaymentStatusHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取已加载的订单对象和支付方式
        OmsOrder fullOrder = context.getAttribute("fullOrder");
        Integer payType = context.getAttribute("payType");

        // 2. 更新订单状态为"待发货"
        fullOrder.setStatus(1);

        // 3. 记录支付时间
        fullOrder.setPaymentTime(new Date());

        // 4. 记录支付方式
        fullOrder.setPayType(payType);

        // 5. 持久化更新到数据库
        orderMapper.updateById(fullOrder);

        // 6. 传递给下一个处理器
        handleNext(context);
    }
}
