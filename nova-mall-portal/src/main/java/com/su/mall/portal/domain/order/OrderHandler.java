package com.su.mall.portal.domain.order;

/**
 * 订单创建责任链抽象处理者
 */
public abstract class OrderHandler {

    protected OrderHandler next;

    public void setNext(OrderHandler next) {
        this.next = next;
    }

    /**
     * 处理订单创建的某个步骤
     */
    public abstract void handle(OrderHandlerContext context);

    /**
     * 调用下一个处理器
     */
    protected void handleNext(OrderHandlerContext context) {
        if (next != null) {
            next.handle(context);
        }
    }
}