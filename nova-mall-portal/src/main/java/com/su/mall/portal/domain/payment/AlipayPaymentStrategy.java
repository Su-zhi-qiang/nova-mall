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
 * <p>支持电脑网站支付（PC端）、手机网站支付（H5端）、异步回调处理和交易状态查询
 * <p>回调处理流程：验签 → 判断交易状态 → 调用订单服务完成支付确认
 *
 * @see PaymentStrategy
 * @see AlipayConfig
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentStrategy implements PaymentStrategy {

    private final AlipayConfig alipayConfig;
    private final AlipayClient alipayClient;
    private final OmsPortalOrderService portalOrderService;

    /**
     * 生成电脑网站支付页面
     *
     * @param param 支付参数（订单号、金额、标题）
     * @return 支付宝收银台页面HTML，异常时返回null
     */
    @Override
    public String pay(PaymentParam param) {
        // 1. 创建电脑网站支付请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        // 2. 设置异步通知地址（支付成功后支付宝回调此地址）
        if (StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())) {
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }

        // 3. 设置同步跳转地址（用户支付完成后浏览器跳转）
        if (StrUtil.isNotEmpty(alipayConfig.getReturnUrl())) {
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }

        // 4. 构建业务参数JSON（订单号、金额、标题、产品码）
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", param.getOutTradeNo());
        bizContent.put("total_amount", param.getTotalAmount().toPlainString());
        bizContent.put("subject", param.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        // 5. 调用支付宝API获取支付页面HTML
        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝电脑支付异常", e);
            return null;
        }
    }

    /**
     * 生成手机网站支付页面
     *
     * @param param 支付参数
     * @return 手机支付页面HTML，异常时返回null
     */
    @Override
    public String webPay(PaymentParam param) {
        // 1. 创建手机网站支付请求对象
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();

        // 2. 设置回调地址（与电脑支付共用同一套回调配置）
        if (StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())) {
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if (StrUtil.isNotEmpty(alipayConfig.getReturnUrl())) {
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }

        // 3. 构建业务参数（产品码为手机支付专用的 QUICK_WAP_WAY）
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", param.getOutTradeNo());
        bizContent.put("total_amount", param.getTotalAmount().toPlainString());
        bizContent.put("subject", param.getSubject());
        bizContent.put("product_code", "QUICK_WAP_WAY");
        request.setBizContent(bizContent.toString());

        // 4. 调用支付宝API获取手机支付页面HTML
        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝手机支付异常", e);
            return null;
        }
    }

    /**
     * 处理支付宝异步回调通知
     * <p>处理流程：验证RSA签名 → 检查交易状态(TRADE_SUCCESS) → 更新订单为已支付
     *
     * @param params 支付宝推送的回调参数
     * @return "success"表示处理成功，"failure"表示失败
     */
    @Override
    public String notify(Map<String, String> params) {
        String result = "failure";

        // 1. 验证回调签名，防止伪造请求
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(), alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付宝回调签名校验异常", e);
        }

        // 2. 签名验证通过后，检查交易状态
        if (signVerified) {
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                result = "success";
                log.info("支付宝回调成功，tradeStatus:{}", tradeStatus);

                // 3. 获取商户订单号，调用订单服务完成支付确认
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

    /**
     * 查询支付宝交易状态
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    支付宝交易流水号
     * @return 交易状态（如 "TRADE_SUCCESS"），查询失败返回null
     */
    @Override
    public String query(String outTradeNo, String tradeNo) {
        // 1. 构建交易查询请求
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

        // 2. 调用支付宝查询接口
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝账单异常", e);
        }

        // 3. 查询成功且交易状态为成功时，同步更新订单状态
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
