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
 * 订单取消链 - 第1步：加载并校验订单
 * <p>查询订单是否存在、是否属于当前用户、状态是否支持取消（待付款0或待发货1）
 * <p>记录原始状态，供后续判断是否需要恢复已支付的库存
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class LoadAndValidateCancelOrderHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取订单ID
        Long orderId = context.getAttribute("cancelOrderId");

        // 2. 查询订单是否存在且未被删除
        OmsOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            Asserts.fail("订单不存在！");
        }

        // 3. 校验订单是否属于当前用户，且状态支持取消（待付款或待发货）
        Long memberId = order.getMemberId();
        List<OmsOrder> cancelOrderList = orderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getId, orderId)
                        .eq(OmsOrder::getMemberId, memberId)
                        .in(OmsOrder::getStatus, 0, 1)          // 只允许取消待付款或待发货的订单
                        .eq(OmsOrder::getDeleteStatus, 0));

        if (CollectionUtils.isEmpty(cancelOrderList)) {
            Asserts.fail("该订单状态不支持取消！");
        }

        // 4. 记录待取消的订单和原始状态
        OmsOrder cancelOrder = cancelOrderList.get(0);
        context.putAttribute("cancelOrder", cancelOrder);
        context.putAttribute("originalStatus", cancelOrder.getStatus()); // 0=待付款，1=待发货

        // 5. 传递给下一个处理器
        handleNext(context);
    }
}
