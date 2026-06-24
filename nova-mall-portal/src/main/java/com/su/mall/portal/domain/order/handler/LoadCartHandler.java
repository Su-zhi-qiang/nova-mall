package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.service.OmsCartItemService;
import com.su.mall.portal.service.UmsMemberService;
import com.su.mall.model.UmsMember;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LoadCartHandler extends OrderHandler {

    private final UmsMemberService memberService;
    private final OmsCartItemService cartItemService;

    @Override
    public void handle(OrderHandlerContext context) {
        UmsMember currentMember = memberService.getCurrentMember();
        context.setCurrentMember(currentMember);
        @SuppressWarnings("unchecked")
        List<Long> cartIds = context.getAttribute("cartIds");
        List<CartPromotionItem> cartPromotionItemList =
                cartItemService.listPromotion(currentMember.getId(), cartIds);
        context.setCartPromotionItemList(cartPromotionItemList);

        for (CartPromotionItem item : cartPromotionItemList) {
            if (item.getFlashPromotion() != null && item.getFlashPromotion()) {
                context.setHasFlashPromotion(true);
                break;
            }
        }
        handleNext(context);
    }
}