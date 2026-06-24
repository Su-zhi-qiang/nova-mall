package com.su.mall.portal.domain.payment;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.su.mall.portal.config.AlipayConfig;
import com.su.mall.portal.service.OmsPortalOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付宝支付策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentStrategy implements PaymentStrategy {

    private final AlipayConfig alipayConfig;
    private final AlipayClient alipayClient;
    private final OmsPortalOrderService portalOrderService;

    @Override
    public String pay(PaymentParam param) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if (StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())) {
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if (StrUtil.isNotEmpty(alipayConfig.getReturnUrl())) {
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", param.getOutTradeNo());
        bizContent.put("total_amount", param.getTotalAmount().toPlainString());
        bizContent.put("subject", param.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝电脑支付异常", e);
            return null;
        }
    }

    @Override
    public String webPay(PaymentParam param) {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        if (StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())) {
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if (StrUtil.isNotEmpty(alipayConfig.getReturnUrl())) {
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", param.getOutTradeNo());
        bizContent.put("total_amount", param.getTotalAmount().toPlainString());
        bizContent.put("subject", param.getSubject());
        bizContent.put("product_code", "QUICK_WAP_WAY");
        request.setBizContent(bizContent.toString());

        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝手机支付异常", e);
            return null;
        }
    }

    @Override
    public String notify(Map<String, String> params) {
        String result = "failure";
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(), alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付宝回调签名校验异常", e);
        }

        if (signVerified) {
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                result = "success";
                log.info("支付宝回调成功，tradeStatus:{}", tradeStatus);
                String outTradeNo = params.get("out_trade_no");
                portalOrderService.paySuccessByOrderSn(outTradeNo, 1);
            } else {
                log.warn("支付宝订单未支付成功，trade_status:{}", tradeStatus);
            }
        } else {
            log.warn("支付宝回调签名校验失败");
        }
        return result;
    }

    @Override
    public String query(String outTradeNo, String tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        if (StrUtil.isNotEmpty(outTradeNo)) {
            bizContent.put("out_trade_no", outTradeNo);
        }
        if (StrUtil.isNotEmpty(tradeNo)) {
            bizContent.put("trade_no", tradeNo);
        }
        String[] queryOptions = {"trade_settle_info"};
        bizContent.put("query_options", queryOptions);
        request.setBizContent(bizContent.toString());

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝账单异常", e);
        }

        if (response != null && response.isSuccess()) {
            log.info("查询支付宝账单成功");
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())) {
                portalOrderService.paySuccessByOrderSn(outTradeNo, 1);
            }
        } else {
            log.error("查询支付宝账单失败");
        }
        return response != null ? response.getTradeStatus() : null;
    }
}