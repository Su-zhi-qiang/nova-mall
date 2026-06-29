package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.portal.dao.PortalOrderItemDao;
import com.su.mall.mapper.OmsOrderMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 订单创建链 - 第11步：持久化订单到数据库
 * <p>先插入订单主表获取自增ID → 回填订单项的orderId和orderSn → 批量插入订单项
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class PersistOrderHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;
    private final PortalOrderItemDao orderItemDao;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder order = context.getOrder();
        List<OmsOrderItem> orderItemList = context.getOrderItemList();

        // 1. 插入订单主表，获取自增主键ID
        orderMapper.insert(order);

        // 2. 回填订单项的orderId和orderSn（关联主表）
        for (OmsOrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
        }

        // 3. 批量插入订单商品项
        orderItemDao.insertList(orderItemList);

        // 4. 传递给下一个处理器
        handleNext(context);
    }
}
