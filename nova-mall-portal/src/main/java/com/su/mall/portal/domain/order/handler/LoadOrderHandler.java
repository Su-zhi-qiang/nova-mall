package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.mapper.OmsOrderMapper;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * 支付成功链 - 第1步：加载并校验订单
 * <p>查询订单是否存在、状态是否为待付款（status=0），校验失败则中断
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class LoadOrderHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取订单ID
        Long orderId = context.getAttribute("orderId");

        // 2. 根据ID查询订单
        OmsOrder fullOrder = orderMapper.selectById(orderId);
        if (fullOrder == null) {
            Asserts.fail("订单不存在");
        }

        // 3. 校验订单状态必须为"待付款"（status=0）
        if (fullOrder.getStatus() != 0) {
            Asserts.fail("订单状态异常，无法支付");
        }

        // 4. 将完整订单对象存入上下文，供后续Handler使用
        context.putAttribute("fullOrder", fullOrder);

        // 5. 传递给下一个处理器
        handleNext(context);
    }
}
