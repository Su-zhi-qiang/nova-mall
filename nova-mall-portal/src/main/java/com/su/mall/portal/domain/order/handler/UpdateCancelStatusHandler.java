package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.mapper.OmsOrderMapper;
import com.su.mall.mapper.OmsOrderItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 更新订单状态为取消
 */
@RequiredArgsConstructor
public class UpdateCancelStatusHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder cancelOrder = context.getAttribute("cancelOrder");
        cancelOrder.setStatus(4);
        orderMapper.updateById(cancelOrder);

        List<OmsOrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, cancelOrder.getId()));
        context.putAttribute("cancelOrderItemList", orderItemList);
        handleNext(context);
    }
}