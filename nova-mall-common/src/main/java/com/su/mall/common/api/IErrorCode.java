package com.su.mall.common.api;

/**
 * API错误码接口
 * <p>所有错误码枚举（如 {@link ResultCode}）均需实现此接口
 * <p>确保每个错误码都包含数字码和可读消息两个维度
 *
 * @see ResultCode
 */
public interface IErrorCode {
    /**
     * 返回码
     */
    long getCode();

    /**
     * 返回信息
     */
    String getMessage();
}
