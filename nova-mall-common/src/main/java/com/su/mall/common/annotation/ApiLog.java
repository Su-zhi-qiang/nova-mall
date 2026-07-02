package com.su.mall.common.annotation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger API操作日志注解
 * 用于标记需要记录操作日志的接口
 * 
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "接口操作日志")
public @interface ApiLog {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录响应结果
     */
    boolean logResult() default false;

    /**
     * 是否记录执行时间
     */
    boolean logTime() default true;

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 查询
         */
        GET("查询"),
        /**
         * 新增
         */
        POST("新增"),
        /**
         * 更新
         */
        PUT("更新"),
        /**
         * 删除
         */
        DELETE("删除"),
        /**
         * 其他
         */
        OTHER("其他");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
