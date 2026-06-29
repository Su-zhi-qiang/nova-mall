package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.OmsCartItem;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 订单创建链 - 第4步：将购物车商品转换为订单商品
 * <p>遍历购物车促销商品列表，逐个构建OmsOrderItem订单项对象
 * <p>映射关系：购物车商品属性 → 订单商品属性（价格、促销、积分、秒杀关联等）
 *
 * @see OrderHandler
 */
public class BuildOrderItemsHandler extends OrderHandler {

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 创建订单商品列表
        List<OmsOrderItem> orderItemList = new ArrayList<>();

        // 2. 遍历购物车促销商品，逐个转换为订单商品
        for (CartPromotionItem cartPromotionItem : context.getCartPromotionItemList()) {
            OmsOrderItem orderItem = buildOrderItem(cartPromotionItem);
            orderItemList.add(orderItem);
        }

        // 3. 设置到上下文中，供后续Handler使用
        context.setOrderItemList(orderItemList);

        // 4. 传递给下一个处理器
        handleNext(context);
    }

    /**
     * 将单个购物车促销商品转换为订单商品
     *
     * @param cartPromotionItem 购物车促销商品
     * @return 订单商品对象
     */
    private static @NonNull OmsOrderItem buildOrderItem(CartPromotionItem cartPromotionItem) {
        OmsOrderItem orderItem = new OmsOrderItem();

        // 1. 复制商品基础信息
        orderItem.setProductId(cartPromotionItem.getProductId());
        orderItem.setProductName(cartPromotionItem.getProductName());
        orderItem.setProductPic(cartPromotionItem.getProductPic());
        orderItem.setProductAttr(cartPromotionItem.getProductAttr());
        orderItem.setProductBrand(cartPromotionItem.getProductBrand());
        orderItem.setProductSn(cartPromotionItem.getProductSn());

        // 2. 设置价格和数量信息
        orderItem.setProductPrice(cartPromotionItem.getPrice());
        orderItem.setProductQuantity(cartPromotionItem.getQuantity());
        orderItem.setProductSkuId(cartPromotionItem.getProductSkuId());
        orderItem.setProductSkuCode(cartPromotionItem.getProductSkuCode());
        orderItem.setProductCategoryId(cartPromotionItem.getProductCategoryId());

        // 3. 设置促销优惠信息
        orderItem.setPromotionAmount(cartPromotionItem.getReduceAmount());
        orderItem.setPromotionName(cartPromotionItem.getPromotionMessage());

        // 4. 设置赠送积分和成长值（null安全处理）
        orderItem.setGiftIntegration(Objects.requireNonNullElse(cartPromotionItem.getIntegration(), 0));
        orderItem.setGiftGrowth(Objects.requireNonNullElse(cartPromotionItem.getGrowth(), 0));

        // 5. 设置秒杀关联ID（秒杀商品需要）
        orderItem.setFlashPromotionRelationId(cartPromotionItem.getFlashPromotionRelationId());

        return orderItem;
    }
}
