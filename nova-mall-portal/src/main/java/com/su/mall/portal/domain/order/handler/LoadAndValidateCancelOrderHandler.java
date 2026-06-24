package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.mapper.OmsOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 加载并校验取消订单
 */
@RequiredArgsConstructor
public class LoadAndValidateCancelOrderHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        Long orderId = context.getAttribute("cancelOrderId");
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            Asserts.fail("订单不存在！");
        }
        Long memberId = order.getMemberId();
        List<OmsOrder> cancelOrderList = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getId, orderId)
                        .eq(OmsOrder::getMemberId, memberId)
                        .in(OmsOrder::getStatus, 0, 1)
                        .eq(OmsOrder::getDeleteStatus, 0));
        if (CollectionUtils.isEmpty(cancelOrderList)) {
            Asserts.fail("该订单状态不支持取消！");
        }
        OmsOrder cancelOrder = cancelOrderList.get(0);
        context.putAttribute("cancelOrder", cancelOrder);
        context.putAttribute("originalStatus", cancelOrder.getStatus());
        handleNext(context);
    }
}