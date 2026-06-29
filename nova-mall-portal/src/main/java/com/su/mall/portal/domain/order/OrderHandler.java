package com.su.mall.portal.domain.order;

/**
 * 订单创建责任链抽象处理者（责任链模式）
 * <p>每个子类负责订单创建流程中的一个步骤（如：库存校验、优惠计算、订单持久化等）
 * <p>通过 {@link #handleNext(OrderHandlerContext)} 将处理权传递给下一个处理器
 *
 * @see OrderCreationChain
 * @see OrderHandlerContext
 */
public abstract class OrderHandler {

    /** 下一个处理器（由责任链执行器自动设置） */
    protected OrderHandler next;

    /**
     * 设置下一个处理器
     *
     * @param next 链中的下一个处理器
     */
    public void setNext(OrderHandler next) {
        this.next = next;
    }

    /**
     * 处理订单创建的某个步骤
     * <p>子类实现具体处理逻辑，处理完毕后调用 {@link #handleNext} 传递到下一步
     *
     * @param context 订单创建上下文
     */
    public abstract void handle(OrderHandlerContext context);

    /**
     * 将处理权传递给链中的下一个处理器
     *
     * @param context 订单创建上下文
     */
    protected void handleNext(OrderHandlerContext context) {
        if (next != null) {
            next.handle(context);
        }
    }
}
