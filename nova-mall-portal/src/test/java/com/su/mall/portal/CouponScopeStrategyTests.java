package com.su.mall.portal;

import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;
import com.su.mall.portal.domain.coupon.*;
import com.su.mall.model.SmsCoupon;
import com.su.mall.model.SmsCouponProductCategoryRelation;
import com.su.mall.model.SmsCouponProductRelation;
import com.su.mall.model.OmsOrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 优惠券适用范围策略模式单元测试
 */
public class CouponScopeStrategyTests {

    /**
     * 测试工厂获取策略
     */
    @Test
    public void testFactoryGetStrategy() {
        // 全场通用
        CouponScopeStrategy allStrategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.ALL_PRODUCT_TYPE);
        assertTrue(allStrategy instanceof AllProductCouponScopeStrategy);

        // 指定分类
        CouponScopeStrategy categoryStrategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.CATEGORY_TYPE);
        assertTrue(categoryStrategy instanceof CategoryCouponScopeStrategy);

        // 指定商品
        CouponScopeStrategy productStrategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.PRODUCT_TYPE);
        assertTrue(productStrategy instanceof ProductCouponScopeStrategy);

        // null类型返回全场通用策略
        CouponScopeStrategy nullStrategy = CouponScopeStrategyFactory.getStrategy(null);
        assertTrue(nullStrategy instanceof AllProductCouponScopeStrategy);
    }

    /**
     * 测试全场通用优惠券 - 计算符合条件金额
     */
    @Test
    public void testAllProductCalcEligibleAmount() {
        CouponScopeStrategy strategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.ALL_PRODUCT_TYPE);

        List<CartPromotionItem> cartItems = buildCartItems();

        SmsCoupon coupon = new SmsCoupon();
        coupon.setUseType(0);
        SmsCouponHistoryDetail detail = new SmsCouponHistoryDetail();
        detail.setCoupon(coupon);

        BigDecimal amount = strategy.calcEligibleAmount(cartItems, detail);

        // 100*2 + 50*1 = 250
        assertEquals(new BigDecimal("250.000"), amount);
    }

    /**
     * 测试指定分类优惠券 - 计算符合条件金额
     */
    @Test
    public void testCategoryCalcEligibleAmount() {
        CouponScopeStrategy strategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.CATEGORY_TYPE);

        List<CartPromotionItem> cartItems = buildCartItems();

        SmsCoupon coupon = new SmsCoupon();
        coupon.setUseType(1);
        SmsCouponHistoryDetail detail = new SmsCouponHistoryDetail();
        detail.setCoupon(coupon);

        // 分类1下的商品
        List<SmsCouponProductCategoryRelation> categoryRelations = new ArrayList<>();
        SmsCouponProductCategoryRelation relation = new SmsCouponProductCategoryRelation();
        relation.setProductCategoryId(1L);
        categoryRelations.add(relation);
        detail.setCategoryRelationList(categoryRelations);

        BigDecimal amount = strategy.calcEligibleAmount(cartItems, detail);

        // 只有商品1(分类1)符合条件: 100*2 = 200
        assertEquals(new BigDecimal("200.000"), amount);
    }

    /**
     * 测试指定商品优惠券 - 计算符合条件金额
     */
    @Test
    public void testProductCalcEligibleAmount() {
        CouponScopeStrategy strategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.PRODUCT_TYPE);

        List<CartPromotionItem> cartItems = buildCartItems();

        SmsCoupon coupon = new SmsCoupon();
        coupon.setUseType(2);
        SmsCouponHistoryDetail detail = new SmsCouponHistoryDetail();
        detail.setCoupon(coupon);

        // 指定商品1
        List<SmsCouponProductRelation> productRelations = new ArrayList<>();
        SmsCouponProductRelation relation = new SmsCouponProductRelation();
        relation.setProductId(1L);
        productRelations.add(relation);
        detail.setProductRelationList(productRelations);

        BigDecimal amount = strategy.calcEligibleAmount(cartItems, detail);

        // 只有商品1符合条件: 100*2 = 200
        assertEquals(new BigDecimal("200.000"), amount);
    }

    /**
     * 测试全场通用优惠券 - 筛选订单商品
     */
    @Test
    public void testAllProductFilterOrderItems() {
        CouponScopeStrategy strategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.ALL_PRODUCT_TYPE);

        List<OmsOrderItem> orderItems = buildOrderItems();
        SmsCouponHistoryDetail detail = new SmsCouponHistoryDetail();

        List<OmsOrderItem> result = strategy.filterOrderItems(detail, orderItems);

        // 全部商品都符合
        assertEquals(3, result.size());
    }

    /**
     * 测试指定分类优惠券 - 筛选订单商品
     */
    @Test
    public void testCategoryFilterOrderItems() {
        CouponScopeStrategy strategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.CATEGORY_TYPE);

        List<OmsOrderItem> orderItems = buildOrderItems();
        SmsCouponHistoryDetail detail = new SmsCouponHistoryDetail();

        List<SmsCouponProductCategoryRelation> categoryRelations = new ArrayList<>();
        SmsCouponProductCategoryRelation relation = new SmsCouponProductCategoryRelation();
        relation.setProductCategoryId(1L);
        categoryRelations.add(relation);
        detail.setCategoryRelationList(categoryRelations);

        List<OmsOrderItem> result = strategy.filterOrderItems(detail, orderItems);

        // 只有分类1的商品符合
        assertEquals(2, result.size());
        // 不符合的商品couponAmount应为0
        for (OmsOrderItem item : orderItems) {
            if (!result.contains(item)) {
                assertEquals(new BigDecimal("0"), item.getCouponAmount());
            }
        }
    }

    /**
     * 测试指定商品优惠券 - 筛选订单商品
     */
    @Test
    public void testProductFilterOrderItems() {
        CouponScopeStrategy strategy = CouponScopeStrategyFactory.getStrategy(CouponScopeStrategyFactory.PRODUCT_TYPE);

        List<OmsOrderItem> orderItems = buildOrderItems();
        SmsCouponHistoryDetail detail = new SmsCouponHistoryDetail();

        List<SmsCouponProductRelation> productRelations = new ArrayList<>();
        SmsCouponProductRelation relation = new SmsCouponProductRelation();
        relation.setProductId(1L);
        productRelations.add(relation);
        detail.setProductRelationList(productRelations);

        List<OmsOrderItem> result = strategy.filterOrderItems(detail, orderItems);

        // 只有商品1符合
        assertEquals(1, result.size());
    }

    private List<CartPromotionItem> buildCartItems() {
        List<CartPromotionItem> items = new ArrayList<>();

        CartPromotionItem item1 = new CartPromotionItem();
        item1.setProductId(1L);
        item1.setProductCategoryId(1L);
        item1.setPrice(new BigDecimal("100.00"));
        item1.setReduceAmount(new BigDecimal("0"));
        item1.setQuantity(2);
        items.add(item1);

        CartPromotionItem item2 = new CartPromotionItem();
        item2.setProductId(2L);
        item2.setProductCategoryId(1L);
        item2.setPrice(new BigDecimal("50.00"));
        item2.setReduceAmount(new BigDecimal("0"));
        item2.setQuantity(1);
        items.add(item2);

        CartPromotionItem item3 = new CartPromotionItem();
        item3.setProductId(3L);
        item3.setProductCategoryId(2L);
        item3.setPrice(new BigDecimal("200.00"));
        item3.setReduceAmount(new BigDecimal("0"));
        item3.setQuantity(1);
        items.add(item3);

        return items;
    }

    private List<OmsOrderItem> buildOrderItems() {
        List<OmsOrderItem> items = new ArrayList<>();

        OmsOrderItem item1 = new OmsOrderItem();
        item1.setProductId(1L);
        item1.setProductCategoryId(1L);
        item1.setProductPrice(new BigDecimal("100.00"));
        item1.setProductQuantity(2);
        items.add(item1);

        OmsOrderItem item2 = new OmsOrderItem();
        item2.setProductId(2L);
        item2.setProductCategoryId(1L);
        item2.setProductPrice(new BigDecimal("50.00"));
        item2.setProductQuantity(1);
        items.add(item2);

        OmsOrderItem item3 = new OmsOrderItem();
        item3.setProductId(3L);
        item3.setProductCategoryId(2L);
        item3.setProductPrice(new BigDecimal("200.00"));
        item3.setProductQuantity(1);
        items.add(item3);

        return items;
    }
}