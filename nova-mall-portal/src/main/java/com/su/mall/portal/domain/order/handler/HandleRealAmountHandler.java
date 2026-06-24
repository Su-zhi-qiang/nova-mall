package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 计算订单商品实付金额
 */
public class HandleRealAmountHandler extends OrderHandler {

    @Override
    public void handle(OrderHandlerContext context) {
        List<OmsOrderItem> orderItemList = context.getOrderItemList();
        for (OmsOrderItem orderItem : orderItemList) {
            BigDecimal realAmount = orderItem.getProductPrice()
                    .subtract(orderItem.getPromotionAmount())
                    .subtract(orderItem.getCouponAmount())
                    .subtract(orderItem.getIntegrationAmount());
            orderItem.setRealAmount(realAmount);
        }
        handleNext(context);
    }
}