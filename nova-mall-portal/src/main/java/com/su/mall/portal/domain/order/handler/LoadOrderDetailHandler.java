package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.portal.domain.OmsOrderDetail;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 加载订单详情
 */
@RequiredArgsConstructor
public class LoadOrderDetailHandler extends OrderHandler {

    private final PortalOrderDao portalOrderDao;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder fullOrder = context.getAttribute("fullOrder");
        OmsOrderDetail orderDetail = portalOrderDao.getDetail(fullOrder.getId());
        if (orderDetail == null || CollectionUtils.isEmpty(orderDetail.getOrderItemList())) {
            Asserts.fail("订单详情为空");
        }
        context.putAttribute("orderItemList", orderDetail.getOrderItemList());
        handleNext(context);
    }
}