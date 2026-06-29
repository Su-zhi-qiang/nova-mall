package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建链 - 第8步：计算订单商品实付金额
 * <p>公式：实付金额 = 商品单价 - 促销优惠 - 优惠券抵扣 - 积分抵扣
 *
 * @see OrderHandler
 */
public class HandleRealAmountHandler extends OrderHandler {

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取订单商品列表
        List<OmsOrderItem> orderItemList = context.getOrderItemList();

        // 2. 遍历每个订单商品，计算实付金额
        for (OmsOrderItem orderItem : orderItemList) {
            BigDecimal realAmount = orderItem.getProductPrice()
                    .subtract(orderItem.getPromotionAmount())    // 减去促销优惠
                    .subtract(orderItem.getCouponAmount())      // 减去优惠券抵扣
                    .subtract(orderItem.getIntegrationAmount()); // 减去积分抵扣
            orderItem.setRealAmount(realAmount);
        }

        // 3. 传递给下一个处理器
        handleNext(context);
    }
}
