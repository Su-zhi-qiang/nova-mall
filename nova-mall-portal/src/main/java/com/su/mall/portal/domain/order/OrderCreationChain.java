package com.su.mall.portal.domain.order;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单创建责任链执行器（责任链模式）
 * <p>将多个订单处理步骤（校验、库存扣减、优惠计算等）串联成链
 * <p>通过 {@link #addHandler(OrderHandler)} 注册处理器，{@link #execute(OrderHandlerContext)} 执行整条链
 *
 * @see OrderHandler 抽象处理器
 * @see OrderHandlerContext 链上下文
 */
public class OrderCreationChain {

    /** 已注册的处理器列表 */
    private final List<OrderHandler> handlers = new ArrayList<>();

    /**
     * 向责任链末尾添加处理器
     *
     * @param handler 处理器实例
     * @return 当前链实例（支持链式调用）
     */
    public OrderCreationChain addHandler(OrderHandler handler) {
        handlers.add(handler);
        return this;
    }

    /**
     * 构建责任链并从第一个处理器开始执行
     * <p>自动将相邻处理器通过 setNext 串联，形成完整的处理链
     *
     * @param context 订单创建上下文（在处理器间共享数据）
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
