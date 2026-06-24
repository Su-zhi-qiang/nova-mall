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
 * 取消订单时恢复优惠券状态
 */
@RequiredArgsConstructor
public class RestoreCouponForCancelHandler extends OrderHandler {

    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponMapper couponMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder cancelOrder = context.getAttribute("cancelOrder");
        if (cancelOrder.getCouponId() != null) {
            updateCouponStatus(cancelOrder.getCouponId(), cancelOrder.getMemberId(), 0, null, null);
        }
        handleNext(context);
    }

    private void updateCouponStatus(Long couponId, Long memberId, Integer useStatus, Long orderId, String orderSn) {
        List<SmsCouponHistory> couponHistoryList = couponHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getUseStatus, useStatus == 0 ? 1 : 0));
        if (!CollectionUtils.isEmpty(couponHistoryList)) {
            SmsCouponHistory couponHistory = couponHistoryList.get(0);
            couponHistory.setUseStatus(useStatus);
            couponHistory.setOrderId(null);
            couponHistory.setOrderSn(null);
            couponHistoryMapper.updateById(couponHistory);

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