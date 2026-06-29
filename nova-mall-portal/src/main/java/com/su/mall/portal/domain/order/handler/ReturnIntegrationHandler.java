package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;

/**
 * 订单取消链 - 第5步（最后一步）：返还积分
 * <p>将订单使用返还积分加回到用户的积分余额中
 * <p>此Handler是取消链的终端节点，不再调用handleNext
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class ReturnIntegrationHandler extends OrderHandler {

    private final UmsMemberService memberService;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取待取消的订单
        OmsOrder cancelOrder = context.getAttribute("cancelOrder");

        // 2. 如果订单使用了积分，返还给用户
        if (cancelOrder.getUseIntegration() != null) {
            // 3. 查询用户的当前积分
            UmsMember returnMember = memberService.getById(cancelOrder.getMemberId());
            if (returnMember != null) {
                // 4. 将使用返还积分加回到用户余额
                memberService.updateIntegration(cancelOrder.getMemberId(),
                        returnMember.getIntegration() + cancelOrder.getUseIntegration());
            }
        }
    }
}
