package com.su.mall.portal.domain.order;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单创建责任链执行器
 */
public class OrderCreationChain {

    private final List<OrderHandler> handlers = new ArrayList<>();

    public OrderCreationChain addHandler(OrderHandler handler) {
        handlers.add(handler);
        return this;
    }

    /**
     * 构建责任链并执行
     */
    public void execute(OrderHandlerContext context) {
        if (handlers.isEmpty()) {
            return;
        }
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
        handlers.get(0).handle(context);
    }
}