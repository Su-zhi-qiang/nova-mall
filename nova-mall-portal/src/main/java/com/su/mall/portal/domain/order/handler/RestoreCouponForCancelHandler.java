package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.model.SmsCoupon;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.mapper.SmsCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 订单取消链 - 第4步：恢复优惠券状态
 * <p>将优惠券使用状态从"已使用"(1)恢复为"未使用"(0)，同时减少优惠券使用次数
 * <p>清空优惠券历史记录中的订单关联信息
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class RestoreCouponForCancelHandler extends OrderHandler {

    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponMapper couponMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取待取消的订单
        OmsOrder cancelOrder = context.getAttribute("cancelOrder");

        // 2. 如果订单使用了优惠券，恢复优惠券状态
        if (cancelOrder.getCouponId() != null) {
            updateCouponStatus(cancelOrder.getCouponId(), cancelOrder.getMemberId(), 0, null, null);
        }

        // 3. 传递给下一个处理器
        handleNext(context);
    }

    /**
     * 更新优惠券使用状态
     */
    private void updateCouponStatus(Long couponId, Long memberId, Integer useStatus, Long orderId, String orderSn) {
        // 1. 查找该会员的优惠券记录（状态为"已使用"）
        List<SmsCouponHistory> couponHistoryList = couponHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getUseStatus, useStatus == 0 ? 1 : 0));

        if (!CollectionUtils.isEmpty(couponHistoryList)) {
            // 2. 更新优惠券历史记录为"未使用"，清空订单关联信息
            SmsCouponHistory couponHistory = couponHistoryList.get(0);
            couponHistory.setUseStatus(useStatus);
            couponHistory.setOrderId(null);
            couponHistory.setOrderSn(null);
            couponHistoryMapper.updateById(couponHistory);

            // 3. 减少优惠券的使用次数
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
