package com.su.mall.portal.domain.order.handler;

import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.mapper.SmsCouponHistoryMapper;
import com.su.mall.mapper.SmsCouponMapper;
import com.su.mall.model.SmsCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 支付成功链 - 第6步（最后一步）：支付成功后更新优惠券状态
 * <p>将优惠券使用状态从"未使用"(0)更新为"已使用"(1)，并增加优惠券使用次数
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class UpdateCouponStatusForPayHandler extends OrderHandler {

    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponMapper couponMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取订单信息
        OmsOrder fullOrder = context.getAttribute("fullOrder");

        // 2. 如果订单使用了优惠券，更新优惠券状态
        if (fullOrder.getCouponId() != null) {
            updateCouponStatus(fullOrder.getCouponId(), fullOrder.getMemberId(), 1, fullOrder.getId(), fullOrder.getOrderSn());
        }

        // 3. 传递给下一个处理器（链的末端，实际无下一个）
        handleNext(context);
    }

    /**
     * 更新优惠券使用状态
     */
    private void updateCouponStatus(Long couponId, Long memberId, Integer useStatus, Long orderId, String orderSn) {
        // 1. 查找该会员的优惠券记录（状态为"未使用"）
        List<SmsCouponHistory> couponHistoryList = couponHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getUseStatus, useStatus == 0 ? 1 : 0));

        if (!CollectionUtils.isEmpty(couponHistoryList)) {
            // 2. 更新优惠券历史记录为"已使用"
            SmsCouponHistory couponHistory = couponHistoryList.get(0);
            couponHistory.setUseTime(new Date());
            couponHistory.setUseStatus(useStatus);
            couponHistory.setOrderId(orderId);
            couponHistory.setOrderSn(orderSn);
            couponHistoryMapper.updateById(couponHistory);

            // 3. 增加优惠券的使用次数
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
