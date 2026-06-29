package com.su.mall.portal.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson序列化配置
 * <p>设置全局ObjectMapper在序列化时忽略null字段，减少API响应体积
 * <p>返回的JSON中不会包含值为null的字段
 */
@Configuration
public class JacksonConfig {

    /**
     * 注册主ObjectMapper Bean
     * <p>使用NON_NULL策略：值为null的字段不序列化到JSON
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false).serializationInclusion(JsonInclude.Include.NON_NULL).build();
    }
}
