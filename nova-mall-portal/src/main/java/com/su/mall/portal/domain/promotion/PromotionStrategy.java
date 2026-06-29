package com.su.mall.portal.domain.promotion;

import com.su.mall.portal.domain.CartPromotionItem;

/**
 * 促销策略接口（策略模式核心）
 * <p>定义促销计算的统一契约，所有促销类型（单品促销、满减、打折等）均实现此接口
 * <p>通过 {@link PromotionStrategyFactory} 工厂获取具体策略实例
 *
 * @see SingleItemPromotionStrategy 单品促销策略
 * @see FullReductionPromotionStrategy 满减优惠策略
 * @see LadderDiscountPromotionStrategy 阶梯打折策略
 * @see NoPromotionStrategy 无优惠策略
 */
public interface PromotionStrategy {

    /**
     * 计算单个商品的促销信息
     * <p>各实现类根据自身促销规则，计算优惠金额、设置促销描述、库存及积分等信息
     *
     * @param context 促销计算上下文，封装了购物车项、商品信息、促销规则及订单总金额等
     * @return 包含优惠金额、促销描述、库存及积分信息的购物车促销项；不满足促销条件时返回null
     */
    CartPromotionItem calculatePromotion(PromotionContext context);
}
