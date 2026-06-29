package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.service.UmsMemberReceiveAddressService;
import lombok.RequiredArgsConstructor;

/**
 * 订单创建链 - 第1步：校验收货地址
 * <p>检查前端是否传入了收货地址ID，未选择则直接中断流程
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class ValidateAddressHandler extends OrderHandler {

    private final UmsMemberReceiveAddressService memberReceiveAddressService;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 从上下文获取收货地址ID，为null说明用户未选择地址
        if (context.getAttributes().get("addressId") == null) {
            Asserts.fail("请选择收货地址！");
        }

        // 2. 校验通过，传递给下一个处理器
        handleNext(context);
    }
}
