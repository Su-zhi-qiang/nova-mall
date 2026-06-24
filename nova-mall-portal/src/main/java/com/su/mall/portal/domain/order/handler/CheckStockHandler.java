package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;

import java.util.List;

/**
 * 校验购物车商品库存
 */
public class CheckStockHandler extends OrderHandler {

    @Override
    public void handle(OrderHandlerContext context) {
        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();
        for (CartPromotionItem item : cartPromotionItemList) {
            if (item.getRealStock() == null
                    || item.getRealStock() <= 0
                    || item.getRealStock() < item.getQuantity()) {
                Asserts.fail("库存不足，无法下单");
            }
        }
        handleNext(context);
    }
}