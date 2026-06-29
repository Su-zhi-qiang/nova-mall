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
 * 支付成功链 - 第3步：加载订单商品详情
 * <p>根据订单ID查询订单商品列表，供后续库存扣减和销量更新使用
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class LoadOrderDetailHandler extends OrderHandler {

    private final PortalOrderDao portalOrderDao;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取已加载的订单对象
        OmsOrder fullOrder = context.getAttribute("fullOrder");

        // 2. 查询订单详情（包含订单商品列表）
        OmsOrderDetail orderDetail = portalOrderDao.getDetail(fullOrder.getId());
        if (orderDetail == null || CollectionUtils.isEmpty(orderDetail.getOrderItemList())) {
            Asserts.fail("订单详情为空");
        }

        // 3. 将订单商品列表存入上下文，供后续Handler使用
        context.putAttribute("orderItemList", orderDetail.getOrderItemList());

        // 4. 传递给下一个处理器
        handleNext(context);
    }
}
