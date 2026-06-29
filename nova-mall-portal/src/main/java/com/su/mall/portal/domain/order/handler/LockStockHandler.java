package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 订单创建链 - 第9步：锁定库存
 * <p>遍历购物车商品，通过原子SQL增加SKU的锁定库存数量
 * <p>锁定库存用于防止并发超卖，支付成功后转为真实扣减
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class LockStockHandler extends OrderHandler {

    private final PmsSkuStockMapper skuStockMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取购物车促销商品列表
        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();

        // 2. 遍历每个商品，原子增加锁定库存
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            int result = skuStockMapper.increaseLockStock(
                    cartPromotionItem.getProductSkuId(),
                    cartPromotionItem.getQuantity());

            // 3. 锁定失败说明库存不足（DB层面兜底校验）
            if (result == 0) {
                Asserts.fail("库存不足，无法下单");
            }
        }

        // 4. 库存锁定成功，传递给下一个处理器
        handleNext(context);
    }
}
