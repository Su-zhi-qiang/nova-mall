package com.su.mall.portal.domain;

import com.su.mall.model.SmsCoupon;
import com.su.mall.model.SmsCouponHistory;
import com.su.mall.model.SmsCouponProductCategoryRelation;
import com.su.mall.model.SmsCouponProductRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 优惠券领取记录详情
 * <p>继承领取记录基础信息，扩展优惠券详情和关联关系（关联商品/关联分类）
 * <p>用于确认订单页展示可用优惠券及其使用条件
 *
 * @see com.su.mall.portal.service.UmsMemberCouponService#listCart
 */
@Getter
@Setter
public class SmsCouponHistoryDetail extends SmsCouponHistory {

    @Schema(title = "优惠券详细信息（面额、门槛、有效期等）")
    private SmsCoupon coupon;

    @Schema(title = "优惠券关联的商品列表（useType=2时有效）")
    private List<SmsCouponProductRelation> productRelationList;

    @Schema(title = "优惠券关联的商品分类列表（useType=1时有效）")
    private List<SmsCouponProductCategoryRelation> categoryRelationList;
}
