package com.su.mall.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置
 * <p>从 application.yml 的 alipay 前缀读取支付宝SDK所需参数
 * <p>包含网关地址、应用密钥、回调地址、签名算法等
 *
 * @see AlipayClientConfig 支付宝客户端Bean配置
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 支付宝网关地址（正式环境：https://openapi.alipay.com） */
    private String gatewayUrl;

    /** 支付宝应用ID */
    private String appId;

    /** 应用私钥（用于请求签名） */
    private String appPrivateKey;

    /** 支付宝公钥（用于回调验签） */
    private String alipayPublicKey;

    /**
     * 同步回调地址（用户支付完成后浏览器跳转）
     * <p>开发环境示例：http://localhost:8060/#/pages/money/paySuccess
     */
    private String returnUrl;

    /**
     * 异步通知地址（支付宝服务器主动推送支付结果）
     * <p>必须公网可访问，开发环境可用内网穿透工具
     * <p>开发环境示例：http://localhost:8085/alipay/notify
     */
    private String notifyUrl;

    /** 参数返回格式（仅支持JSON） */
    private String format = "JSON";

    /** 请求编码格式 */
    private String charset = "UTF-8";

    /** 签名算法类型（RSA2即SHA256WithRSA） */
    private String signType = "RSA2";
}
