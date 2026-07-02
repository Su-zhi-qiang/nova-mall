package com.su.mall.common.api;

/**
 * 响应状态码接口
 * 
 */
public interface IResultCode extends IErrorCode {

    /**
     * 获取状态码
     */
    long getCode();

    /**
     * 获取消息
     */
    String getMessage();
}
