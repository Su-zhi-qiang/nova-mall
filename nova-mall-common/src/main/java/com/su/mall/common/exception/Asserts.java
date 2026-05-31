package com.su.mall.common.exception;

import com.su.mall.common.api.IErrorCode;

/**
 * 断言处理类，用于抛出各种API异常
 * @author Su
 */
public class Asserts {
    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }
}
