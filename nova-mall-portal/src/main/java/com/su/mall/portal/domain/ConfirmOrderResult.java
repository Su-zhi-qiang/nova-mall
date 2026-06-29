package com.su.mall.portal.domain;

import com.su.mall.model.UmsIntegrationConsumeSetting;
import com.su.mall.model.UmsMemberReceiveAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 确认订单页响应数据
 * <p>包含下单前需要展示的所有信息：促销后的购物车、收货地址、可用优惠券、积分规则、金额汇总
 * <p>由 {@link com.su.mall.portal.service.OmsPortalOrderService#generateConfirmOrder} 构建
 */
@Getter
@Setter
public class ConfirmOrderResult {

    @Schema(title = "促销后的购物车商品列表（含优惠信息）")
    private List<CartPromotionItem> cartPromotionItemList;

    @Schema(title = "用户的收货地址列表")
    private List<UmsMemberReceiveAddress> memberReceiveAddressList;

    @Schema(title = "用户可用优惠券列表")
    private List<SmsCouponHistoryDetail> couponHistoryDetailList;

    @Schema(title = "积分使用规则（多少积分抵扣多少钱）")
    private UmsIntegrationConsumeSetting integrationConsumeSetting;

    @Schema(title = "会员当前持有的积分")
    private Integer memberIntegration;

    @Schema(title = "金额汇总")
    private CalcAmount calcAmount;

    /**
     * 订单金额汇总
     */
    @Getter
    @Setter
    public static class CalcAmount {

        @Schema(title = "订单商品总金额（原价合计）")
        private BigDecimal totalAmount;

        @Schema(title = "运费")
        private BigDecimal freightAmount;

        @Schema(title = "活动优惠金额（促销减免合计）")
        private BigDecimal promotionAmount;

        @Schema(title = "应付金额（总金额 - 优惠 + 运费）")
        private BigDecimal payAmount;
    }
}
