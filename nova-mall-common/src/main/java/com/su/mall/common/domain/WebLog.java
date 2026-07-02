package com.su.mall.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Controller层请求日志封装类
 * <p>由 {@link com.su.mall.common.log.WebLogAspect} 在环绕通知中构建
 * <p>包含请求的完整信息：URL、HTTP方法、参数、响应、耗时、IP、操作用户等
 * <p>通过Logstash以JSON格式推送到Elasticsearch，用于接口监控和性能分析
 */
@Data
@EqualsAndHashCode
public class WebLog {
    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作用户
     */
    private String username;

    /**
     * 操作时间
     */
    private Long startTime;

    /**
     * 消耗时间
     */
    private Integer spendTime;

    /**
     * 根路径
     */
    private String basePath;

    /**
     * URI
     */
    private String uri;

    /**
     * URL
     */
    private String url;

    /**
     * 请求类型
     */
    private String method;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 请求参数
     */
    private Object parameter;

    /**
     * 返回结果
     */
    private Object result;

}
