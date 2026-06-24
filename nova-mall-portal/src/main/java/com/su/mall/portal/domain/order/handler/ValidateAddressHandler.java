package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.service.UmsMemberReceiveAddressService;
import lombok.RequiredArgsConstructor;

/**
 * 校验收货地址
 */
@RequiredArgsConstructor
public class ValidateAddressHandler extends OrderHandler {

    private final UmsMemberReceiveAddressService memberReceiveAddressService;

    @Override
    public void handle(OrderHandlerContext context) {
        if (context.getAttributes().get("addressId") == null) {
            Asserts.fail("请选择收货地址！");
        }
        handleNext(context);
    }
}