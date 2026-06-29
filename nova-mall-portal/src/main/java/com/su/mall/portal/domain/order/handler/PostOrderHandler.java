package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.service.UmsMemberService;
import com.su.mall.portal.service.OmsCartItemService;
import com.su.mall.portal.component.CancelOrderSender;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.mapper.SmsCouponMapper;
import com.su.mall.model.SmsCoupon;
import com.su.mall.portal.domain.CartPromotionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单创建链 - 第12步（最后一步）：订单后处理
 * <p>处理流程：更新优惠券使用状态 → 扣除用户积分 → 删除购物车商品 → 发送延迟取消消息
 * <p>此Handler是责任链的终端节点，不再调用handleNext
 *
 * @see OrderHandler
 * @see CancelOrderSender
 */
@RequiredArgsConstructor
public class PostOrderHandler extends OrderHandler {

    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponMapper couponMapper;
    private final UmsMemberService memberService;
    private final OmsCartItemService cartItemService;
    private final CancelOrderSender cancelOrderSender;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder order = context.getOrder();
        UmsMember currentMember = context.getCurrentMember();
        Integer useIntegration = context.getAttribute("useIntegration");
        Long couponId = context.getAttribute("couponId");
        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();

        // 1. 更新优惠券使用状态为"已使用"
        if (couponId != null) {
            updateCouponStatus(couponId, currentMember.getId(), 1, order.getId(), order.getOrderSn());
        }

        // 2. 扣除用户使用的积分
        if (useIntegration != null) {
            order.setUseIntegration(useIntegration);
            if (currentMember.getIntegration() == null) {
                currentMember.setIntegration(0);
            }
            memberService.updateIntegration(currentMember.getId(), currentMember.getIntegration() - useIntegration);
        }

        // 3. 删除购物车中已下单的商品
        List<Long> ids = new ArrayList<>();
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            ids.add(cartPromotionItem.getId());
        }
        cartItemService.delete(currentMember.getId(), ids);

        // 4. 发送延迟消息到RabbitMQ，超时未支付自动取消订单
        cancelOrderSender.sendMessage(order.getId(), context.getAttribute("delayTimes"));
    }

    /**
     * 更新优惠券使用状态
     *
     * @param couponId  优惠券ID
     * @param memberId  会员ID
     * @param useStatus 目标状态：1=已使用，0=未使用
     * @param orderId   订单ID
     * @param orderSn   订单编号
     */
    private void updateCouponStatus(Long couponId, Long memberId, Integer useStatus, Long orderId, String orderSn) {
        if (couponId == null) {
            return;
        }

        // 1. 查找该会员的指定优惠券记录（状态取反：使用→未使用，或未使用→已使用）
        List<SmsCouponHistory> couponHistoryList = couponHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getUseStatus, useStatus == 0 ? 1 : 0));

        if (!CollectionUtils.isEmpty(couponHistoryList)) {
            // 2. 更新优惠券历史记录状态
            SmsCouponHistory couponHistory = couponHistoryList.get(0);
            couponHistory.setUseTime(new Date());
            couponHistory.setUseStatus(useStatus);
            couponHistory.setOrderId(orderId);
            couponHistory.setOrderSn(orderSn);
            couponHistoryMapper.updateById(couponHistory);

            // 3. 更新优惠券的使用次数
            SmsCoupon coupon = couponMapper.selectById(couponId);
            if (coupon != null) {
                if (useStatus == 1) {
                    coupon.setUseCount(coupon.getUseCount() == null ? 1 : coupon.getUseCount() + 1);
                } else {
                    coupon.setUseCount(coupon.getUseCount() == null ? 0 : Math.max(0, coupon.getUseCount() - 1));
                }
                couponMapper.updateById(coupon);
            }
        }
    }
}
