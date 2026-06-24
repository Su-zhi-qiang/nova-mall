package com.su.mall.portal;

import com.su.mall.portal.domain.order.*;
import com.su.mall.portal.domain.order.handler.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 责任链模式单元测试
 */
public class OrderChainTests {

    /**
     * 测试责任链空链执行
     */
    @Test
    public void testEmptyChain() {
        OrderCreationChain chain = new OrderCreationChain();
        OrderHandlerContext context = new OrderHandlerContext();

        // 不应抛异常
        assertDoesNotThrow(() -> chain.execute(context));
    }

    /**
     * 测试责任链顺序执行
     */
    @Test
    public void testChainExecutionOrder() {
        List<String> executionOrder = new ArrayList<>();

        OrderHandler handler1 = new TestHandler("handler1", executionOrder);
        OrderHandler handler2 = new TestHandler("handler2", executionOrder);
        OrderHandler handler3 = new TestHandler("handler3", executionOrder);

        OrderCreationChain chain = new OrderCreationChain();
        chain.addHandler(handler1);
        chain.addHandler(handler2);
        chain.addHandler(handler3);

        OrderHandlerContext context = new OrderHandlerContext();
        chain.execute(context);

        assertEquals(3, executionOrder.size());
        assertEquals("handler1", executionOrder.get(0));
        assertEquals("handler2", executionOrder.get(1));
        assertEquals("handler3", executionOrder.get(2));
    }

    /**
     * 测试上下文数据共享
     */
    @Test
    public void testContextDataSharing() {
        OrderHandlerContext context = new OrderHandlerContext();
        context.putAttribute("step1Complete", false);
        context.putAttribute("step2Complete", false);

        OrderHandler handler1 = new OrderHandler() {
            @Override
            public void handle(OrderHandlerContext ctx) {
                ctx.putAttribute("step1Complete", true);
                handleNext(ctx);
            }
        };

        OrderHandler handler2 = new OrderHandler() {
            @Override
            public void handle(OrderHandlerContext ctx) {
                Boolean step1 = ctx.getAttribute("step1Complete");
                assertTrue(step1);
                ctx.putAttribute("step2Complete", true);
                handleNext(ctx);
            }
        };

        OrderCreationChain chain = new OrderCreationChain();
        chain.addHandler(handler1);
        chain.addHandler(handler2);
        chain.execute(context);

        assertTrue(context.getAttribute("step1Complete"));
        assertTrue(context.getAttribute("step2Complete"));
    }

    /**
     * 测试单个处理器
     */
    @Test
    public void testSingleHandler() {
        List<String> executionOrder = new ArrayList<>();

        OrderHandler handler = new TestHandler("onlyHandler", executionOrder);

        OrderCreationChain chain = new OrderCreationChain();
        chain.addHandler(handler);

        OrderHandlerContext context = new OrderHandlerContext();
        chain.execute(context);

        assertEquals(1, executionOrder.size());
        assertEquals("onlyHandler", executionOrder.get(0));
    }

    /**
     * 测试BuildOrderItemsHandler - 购物车商品转订单商品
     */
    @Test
    public void testBuildOrderItemsHandler() {
        com.su.mall.portal.domain.CartPromotionItem cartItem = new com.su.mall.portal.domain.CartPromotionItem();
        cartItem.setProductId(1L);
        cartItem.setProductName("测试商品");
        cartItem.setProductPic("test.jpg");
        cartItem.setProductAttr("红色");
        cartItem.setProductBrand("品牌");
        cartItem.setProductSn("SN001");
        cartItem.setPrice(new java.math.BigDecimal("99.99"));
        cartItem.setQuantity(2);
        cartItem.setProductSkuId(100L);
        cartItem.setProductSkuCode("SKU001");
        cartItem.setProductCategoryId(10L);
        cartItem.setReduceAmount(new java.math.BigDecimal("10.00"));
        cartItem.setPromotionMessage("单品促销");
        cartItem.setIntegration(5);
        cartItem.setGrowth(3);

        List<com.su.mall.portal.domain.CartPromotionItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        OrderHandlerContext context = new OrderHandlerContext();
        context.setCartPromotionItemList(cartItems);

        BuildOrderItemsHandler handler = new BuildOrderItemsHandler();
        handler.handle(context);

        List<com.su.mall.model.OmsOrderItem> orderItems = context.getOrderItemList();
        assertNotNull(orderItems);
        assertEquals(1, orderItems.size());

        com.su.mall.model.OmsOrderItem orderItem = orderItems.get(0);
        assertEquals(1L, orderItem.getProductId());
        assertEquals("测试商品", orderItem.getProductName());
        assertEquals(new java.math.BigDecimal("99.99"), orderItem.getProductPrice());
        assertEquals(2, orderItem.getProductQuantity());
        assertEquals(100L, orderItem.getProductSkuId());
        assertEquals(10L, orderItem.getProductCategoryId());
        assertEquals(new java.math.BigDecimal("10.00"), orderItem.getPromotionAmount());
        assertEquals("单品促销", orderItem.getPromotionName());
        assertEquals(5, orderItem.getGiftIntegration());
        assertEquals(3, orderItem.getGiftGrowth());
    }

    /**
     * 测试CheckStockHandler - 库存充足
     */
    @Test
    public void testCheckStockHandlerSuccess() {
        com.su.mall.portal.domain.CartPromotionItem cartItem = new com.su.mall.portal.domain.CartPromotionItem();
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        cartItem.setRealStock(10);

        List<com.su.mall.portal.domain.CartPromotionItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        OrderHandlerContext context = new OrderHandlerContext();
        context.setCartPromotionItemList(cartItems);

        CheckStockHandler handler = new CheckStockHandler();
        assertDoesNotThrow(() -> handler.handle(context));
    }

    /**
     * 测试CheckStockHandler - 库存不足
     */
    @Test
    public void testCheckStockHandlerInsufficient() {
        com.su.mall.portal.domain.CartPromotionItem cartItem = new com.su.mall.portal.domain.CartPromotionItem();
        cartItem.setProductId(1L);
        cartItem.setQuantity(10);
        cartItem.setRealStock(5);

        List<com.su.mall.portal.domain.CartPromotionItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        OrderHandlerContext context = new OrderHandlerContext();
        context.setCartPromotionItemList(cartItems);

        CheckStockHandler handler = new CheckStockHandler();
        assertThrows(Exception.class, () -> handler.handle(context));
    }

    /**
     * 测试HandleRealAmountHandler
     */
    @Test
    public void testHandleRealAmountHandler() {
        com.su.mall.model.OmsOrderItem orderItem = new com.su.mall.model.OmsOrderItem();
        orderItem.setProductPrice(new java.math.BigDecimal("100.00"));
        orderItem.setPromotionAmount(new java.math.BigDecimal("10.00"));
        orderItem.setCouponAmount(new java.math.BigDecimal("5.00"));
        orderItem.setIntegrationAmount(new java.math.BigDecimal("3.00"));

        List<com.su.mall.model.OmsOrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        OrderHandlerContext context = new OrderHandlerContext();
        context.setOrderItemList(orderItems);

        HandleRealAmountHandler handler = new HandleRealAmountHandler();
        handler.handle(context);

        // 100 - 10 - 5 - 3 = 82
        assertEquals(new java.math.BigDecimal("82.000"), orderItem.getRealAmount());
    }

    /**
     * 测试处理器链中断
     */
    @Test
    public void testChainInterruption() {
        List<String> executionOrder = new ArrayList<>();

        OrderHandler handler1 = new OrderHandler() {
            @Override
            public void handle(OrderHandlerContext ctx) {
                executionOrder.add("handler1");
                // 不调用 handleNext，链中断
            }
        };

        OrderHandler handler2 = new TestHandler("handler2", executionOrder);

        OrderCreationChain chain = new OrderCreationChain();
        chain.addHandler(handler1);
        chain.addHandler(handler2);

        OrderHandlerContext context = new OrderHandlerContext();
        chain.execute(context);

        assertEquals(1, executionOrder.size());
        assertEquals("handler1", executionOrder.get(0));
    }

    /**
     * 测试处理器链中断后的上下文状态
     */
    @Test
    public void testChainInterruptionContextState() {
        OrderHandler handler1 = new OrderHandler() {
            @Override
            public void handle(OrderHandlerContext ctx) {
                ctx.putAttribute("step1", true);
                // 不调用 handleNext
            }
        };

        OrderHandler handler2 = new OrderHandler() {
            @Override
            public void handle(OrderHandlerContext ctx) {
                ctx.putAttribute("step2", true);
                handleNext(ctx);
            }
        };

        OrderCreationChain chain = new OrderCreationChain();
        chain.addHandler(handler1);
        chain.addHandler(handler2);

        OrderHandlerContext context = new OrderHandlerContext();
        chain.execute(context);

        assertTrue(context.getAttribute("step1"));
        assertNull(context.getAttribute("step2"));
    }

    /**
     * 测试处理器类
     */
    private static class TestHandler extends OrderHandler {
        private final String name;
        private final List<String> executionOrder;

        public TestHandler(String name, List<String> executionOrder) {
            this.name = name;
            this.executionOrder = executionOrder;
        }

        @Override
        public void handle(OrderHandlerContext context) {
            executionOrder.add(name);
            handleNext(context);
        }
    }
}