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
 * 将购物车商品转换为订单商品
 */
public class BuildOrderItemsHandler extends OrderHandler {

    @Override
    public void handle(OrderHandlerContext context) {
        List<OmsOrderItem> orderItemList = new ArrayList<>();
        for (CartPromotionItem cartPromotionItem : context.getCartPromotionItemList()) {
            OmsOrderItem orderItem = buildOrderItem(cartPromotionItem);
            orderItemList.add(orderItem);
        }
        context.setOrderItemList(orderItemList);
        handleNext(context);
    }

    private static @NonNull OmsOrderItem buildOrderItem(CartPromotionItem cartPromotionItem) {
        OmsOrderItem orderItem = new OmsOrderItem();
        orderItem.setProductId(cartPromotionItem.getProductId());
        orderItem.setProductName(cartPromotionItem.getProductName());
        orderItem.setProductPic(cartPromotionItem.getProductPic());
        orderItem.setProductAttr(cartPromotionItem.getProductAttr());
        orderItem.setProductBrand(cartPromotionItem.getProductBrand());
        orderItem.setProductSn(cartPromotionItem.getProductSn());
        orderItem.setProductPrice(cartPromotionItem.getPrice());
        orderItem.setProductQuantity(cartPromotionItem.getQuantity());
        orderItem.setProductSkuId(cartPromotionItem.getProductSkuId());
        orderItem.setProductSkuCode(cartPromotionItem.getProductSkuCode());
        orderItem.setProductCategoryId(cartPromotionItem.getProductCategoryId());
        orderItem.setPromotionAmount(cartPromotionItem.getReduceAmount());
        orderItem.setPromotionName(cartPromotionItem.getPromotionMessage());
        orderItem.setGiftIntegration(Objects.requireNonNullElse(cartPromotionItem.getIntegration(), 0));
        orderItem.setGiftGrowth(Objects.requireNonNullElse(cartPromotionItem.getGrowth(), 0));
        orderItem.setFlashPromotionRelationId(cartPromotionItem.getFlashPromotionRelationId());
        return orderItem;
    }
}