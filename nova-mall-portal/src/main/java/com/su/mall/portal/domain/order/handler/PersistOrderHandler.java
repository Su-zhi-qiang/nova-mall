package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.portal.dao.PortalOrderItemDao;
import com.su.mall.mapper.OmsOrderMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PersistOrderHandler extends OrderHandler {

    private final OmsOrderMapper orderMapper;
    private final PortalOrderItemDao orderItemDao;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder order = context.getOrder();
        List<OmsOrderItem> orderItemList = context.getOrderItemList();

        orderMapper.insert(order);
        for (OmsOrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
        }
        orderItemDao.insertList(orderItemList);
        handleNext(context);
    }
}