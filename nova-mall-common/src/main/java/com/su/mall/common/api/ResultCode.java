package com.su.mall.common.api;

import lombok.Getter;

/**
 * API返回码枚举
 * <p>统一定义所有HTTP接口的业务状态码和提示信息
 * <p>状态码规范：200=成功, 400=业务异常, 401=未认证, 403=无权限, 404=参数错误, 500=操作失败, 501=系统异常, 502=空指针
 *
 * @see CommonResult 统一返回结果封装
 * @see IErrorCode 错误码接口
 */
@Getter
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限"),
    BUSINESS_ERROR(400, "业务异常"),
    SYSTEM_ERROR(501, "系统异常"),
    NULL_POINTER_ERROR(502, "空指针异常");
    private final long code;
    private final String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

}