package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LockStockHandler extends OrderHandler {

    private final PmsSkuStockMapper skuStockMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            int result = skuStockMapper.increaseLockStock(cartPromotionItem.getProductSkuId(), cartPromotionItem.getQuantity());
            if (result == 0) {
                Asserts.fail("库存不足，无法下单");
            }
        }
        handleNext(context);
    }
}