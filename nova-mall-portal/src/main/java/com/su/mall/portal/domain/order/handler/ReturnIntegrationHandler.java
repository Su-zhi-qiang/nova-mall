package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;

/**
 * 取消订单时返还积分
 */
@RequiredArgsConstructor
public class ReturnIntegrationHandler extends OrderHandler {

    private final UmsMemberService memberService;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder cancelOrder = context.getAttribute("cancelOrder");
        if (cancelOrder.getUseIntegration() != null) {
            UmsMember returnMember = memberService.getById(cancelOrder.getMemberId());
            if (returnMember != null) {
                memberService.updateIntegration(cancelOrder.getMemberId(),
                        returnMember.getIntegration() + cancelOrder.getUseIntegration());
            }
        }
    }
}