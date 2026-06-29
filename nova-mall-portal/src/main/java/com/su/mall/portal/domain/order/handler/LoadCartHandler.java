package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.service.OmsCartItemService;
import com.su.mall.portal.service.UmsMemberService;
import com.su.mall.model.UmsMember;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 订单创建链 - 第2步：加载购物车商品
 * <p>获取当前登录会员信息，查询购物车中选中商品的促销信息
 * <p>同时检测购物车中是否包含秒杀商品，为后续秒杀校验做准备
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class LoadCartHandler extends OrderHandler {

    private final UmsMemberService memberService;
    private final OmsCartItemService cartItemService;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取当前登录会员信息，设置到上下文中供后续Handler使用
        UmsMember currentMember = memberService.getCurrentMember();
        context.setCurrentMember(currentMember);

        // 2. 从上下文中获取前端传入的购物车ID列表
        @SuppressWarnings("unchecked")
        List<Long> cartIds = context.getAttribute("cartIds");

        // 3. 查询购物车商品并计算促销优惠信息
        List<CartPromotionItem> cartPromotionItemList =
                cartItemService.listPromotion(currentMember.getId(), cartIds);
        context.setCartPromotionItemList(cartPromotionItemList);

        // 4. 遍历购物车商品，检测是否包含秒杀商品
        for (CartPromotionItem item : cartPromotionItemList) {
            if (item.getFlashPromotion() != null && item.getFlashPromotion()) {
                context.setHasFlashPromotion(true);
                break;
            }
        }

        // 5. 传递给下一个处理器
        handleNext(context);
    }
}
