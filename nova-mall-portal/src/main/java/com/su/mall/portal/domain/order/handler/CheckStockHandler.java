package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;

import java.util.List;

/**
 * 订单创建链 - 第5步：校验商品库存
 * <p>遍历购物车商品，检查每个商品的可用库存是否充足
 * <p>库存不足时直接中断下单流程
 *
 * @see OrderHandler
 */
public class CheckStockHandler extends OrderHandler {

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取购物车促销商品列表
        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();

        // 2. 遍历每个商品，校验库存是否充足
        for (CartPromotionItem item : cartPromotionItemList) {
            if (item.getRealStock() == null
                    || item.getRealStock() <= 0
                    || item.getRealStock() < item.getQuantity()) {
                Asserts.fail("库存不足，无法下单");
            }
        }

        // 3. 库存校验通过，传递给下一个处理器
        handleNext(context);
    }
}
