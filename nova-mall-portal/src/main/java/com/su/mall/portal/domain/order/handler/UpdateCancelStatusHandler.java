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
 * 订单取消链 - 第2步：更新订单状态为已取消
 * <p>将订单状态更新为4（已关闭），同时查询订单商品列表供后续库存恢复使用
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class UpdateCancelStatusHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取待取消的订单
        OmsOrder cancelOrder = context.getAttribute("cancelOrder");

        // 2. 更新订单状态为"已关闭"(status=4)
        cancelOrder.setStatus(4);
        orderMapper.updateById(cancelOrder);

        // 3. 查询订单商品列表（供后续恢复库存使用）
        List<OmsOrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, cancelOrder.getId()));
        context.putAttribute("cancelOrderItemList", orderItemList);

        // 4. 传递给下一个处理器
        handleNext(context);
    }
}
