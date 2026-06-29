package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.mapper.UmsIntegrationConsumeSettingMapper;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.UmsIntegrationConsumeSetting;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 订单创建链 - 第7步：处理积分抵扣
 * <p>校验积分使用规则（使用门槛、与优惠券互斥、最大抵扣比例）→ 按比例分摊积分抵扣金额
 * <p>分摊算法：integrationAmount = (商品单价 / 总价) × 积分抵扣金额
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class HandleIntegrationHandler extends OrderHandler {

    private final UmsIntegrationConsumeSettingMapper integrationConsumeSettingMapper;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取用户要使用的积分数
        Integer useIntegration = context.getAttribute("useIntegration");
        List<OmsOrderItem> orderItemList = context.getOrderItemList();

        // 2. 检查是否使用了优惠券（积分与优惠券可能互斥）
        boolean hasCoupon = context.getAttribute("couponId") != null;

        // 3. 未使用积分时，所有订单项的积分抵扣金额设为0
        if (useIntegration == null || useIntegration.equals(0)) {
            for (OmsOrderItem orderItem : orderItemList) {
                orderItem.setIntegrationAmount(new BigDecimal(0));
            }
        } else {
            // 4. 计算订单总价
            BigDecimal totalAmount = calcTotalAmount(orderItemList);

            // 5. 根据积分规则计算可用的积分抵扣金额
            BigDecimal integrationAmount = getUseIntegrationAmount(useIntegration, totalAmount,
                    context.getCurrentMember(), hasCoupon);

            if (integrationAmount.compareTo(new BigDecimal(0)) == 0) {
                Asserts.fail("积分不可用");
            } else {
                // 6. 按比例分摊积分抵扣金额到每个订单商品
                for (OmsOrderItem orderItem : orderItemList) {
                    BigDecimal perAmount = orderItem.getProductPrice()
                            .divide(totalAmount, 3, RoundingMode.HALF_EVEN)
                            .multiply(integrationAmount);
                    orderItem.setIntegrationAmount(perAmount);
                }
            }
        }

        // 7. 传递给下一个处理器
        handleNext(context);
    }

    /**
     * 计算订单总价 = Σ(单价 × 数量)
     */
    private BigDecimal calcTotalAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsOrderItem item : orderItemList) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getProductQuantity())));
        }
        return totalAmount;
    }

    /**
     * 根据积分消费规则计算积分抵扣金额
     * <p>规则校验：使用积分不能超过持有量、不能低于使用门槛、不能超过订单最大抵扣比例
     * <p>使用了优惠券且规则不允许同时使用时返回0
     */
    private BigDecimal getUseIntegrationAmount(Integer useIntegration, BigDecimal totalAmount,
                                               com.su.mall.model.UmsMember currentMember, boolean hasCoupon) {
        BigDecimal zeroAmount = new BigDecimal(0);

        // 1. 校验：使用积分不能超过用户持有积分
        if (useIntegration.compareTo(currentMember.getIntegration()) > 0) {
            return zeroAmount;
        }

        // 2. 获取积分消费规则配置
        UmsIntegrationConsumeSetting integrationConsumeSetting = integrationConsumeSettingMapper.selectById(1L);
        if (integrationConsumeSetting == null) {
            return zeroAmount;
        }

        // 3. 校验：使用了优惠券且规则不允许同时使用
        if (hasCoupon && integrationConsumeSetting.getCouponStatus().equals(0)) {
            return zeroAmount;
        }

        // 4. 校验：使用积分不能低于最低使用门槛
        if (useIntegration.compareTo(integrationConsumeSetting.getUseUnit()) < 0) {
            return zeroAmount;
        }

        // 5. 计算积分抵扣金额 = 使用积分 / 每单位积分抵扣金额
        BigDecimal integrationAmount = new BigDecimal(useIntegration)
                .divide(new BigDecimal(integrationConsumeSetting.getUseUnit()), 2, RoundingMode.HALF_EVEN);

        // 6. 校验：积分抵扣金额不能超过订单最大抵扣比例
        BigDecimal maxPercent = new BigDecimal(integrationConsumeSetting.getMaxPercentPerOrder())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN);
        if (integrationAmount.compareTo(totalAmount.multiply(maxPercent)) > 0) {
            return zeroAmount;
        }

        return integrationAmount;
    }
}
