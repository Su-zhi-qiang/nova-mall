package com.su.mall.portal.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.portal.domain.AliPayParam;
import com.su.mall.portal.domain.payment.PaymentParam;
import com.su.mall.portal.domain.payment.PaymentStrategy;
import com.su.mall.portal.domain.payment.PaymentStrategyFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 支付Controller（通过策略工厂路由到具体支付方式）
 */
@RestController
@Tag(name = "PaymentController", description = "支付相关接口")
@RequestMapping("/pay")
@RequiredArgsConstructor
public class AlipayController {

    private final PaymentStrategyFactory paymentStrategyFactory;

    private PaymentParam buildParam(AliPayParam aliPayParam, Integer payType) {
        return PaymentParam.builder()
                .outTradeNo(aliPayParam.getOutTradeNo())
                .totalAmount(aliPayParam.getTotalAmount())
                .subject(aliPayParam.getSubject())
                .payType(payType)
                .build();
    }

    @Operation(summary = "电脑网站支付")
    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public void pay(AliPayParam aliPayParam,
                    @RequestParam(defaultValue = "1") Integer payType,
                    HttpServletResponse response) throws IOException {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(payType);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(strategy.pay(buildParam(aliPayParam, payType)));
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Operation(summary = "手机网站支付")
    @RequestMapping(value = "/webPay", method = RequestMethod.GET)
    public void webPay(AliPayParam aliPayParam,
                       @RequestParam(defaultValue = "1") Integer payType,
                       HttpServletResponse response) throws IOException {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(payType);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(strategy.webPay(buildParam(aliPayParam, payType)));
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Operation(summary = "支付异步回调", description = "必须为POST请求，执行成功返回success，执行失败返回failure")
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public String notify(@RequestParam("payType") Integer payType, Map<String, String> params) {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(payType);
        return strategy.notify(params);
    }

    @Operation(summary = "查询交易状态", description = "订单支付成功返回交易状态：TRADE_SUCCESS")
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public CommonResult<String> query(String outTradeNo, String tradeNo,
                                      @RequestParam(defaultValue = "1") Integer payType) {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(payType);
        return CommonResult.success(strategy.query(outTradeNo, tradeNo));
    }
}