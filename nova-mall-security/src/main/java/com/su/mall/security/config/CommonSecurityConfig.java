package com.su.mall.security.config;

import com.su.mall.security.component.*;
import com.su.mall.security.util.JwtTokenUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security通用Bean配置
 * <p>注册安全模块所需的所有通用Bean：密码编码器、JWT工具、过滤器、异常处理器
 * <p>动态权限相关Bean通过@ConditionalOnBean条件装配（需要DynamicSecurityService时才创建）
 */
@Configuration
public class CommonSecurityConfig {

    /**
     * 密码编码器（BCrypt加密）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 白名单URL配置
     */
    @Bean
    public IgnoreUrlsConfig ignoreUrlsConfig() {
        return new IgnoreUrlsConfig();
    }

    /**
     * JWT Token工具类
     */
    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }

    /**
     * 权限不足异常处理器（返回403 JSON）
     */
    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler() {
        return new RestfulAccessDeniedHandler();
    }

    /**
     * 未认证异常处理器（返回401 JSON）
     */
    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    /**
     * JWT认证过滤器（每个请求校验Token）
     */
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter(UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        return new JwtAuthenticationTokenFilter(userDetailsService, jwtTokenUtil);
    }

    /**
     * 动态权限元数据源（条件装配：需要DynamicSecurityService Bean时才创建）
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicSecurityMetadataSource dynamicSecurityMetadataSource(DynamicSecurityService dynamicSecurityService) {
        return new DynamicSecurityMetadataSource(dynamicSecurityService);
    }

    /**
     * 动态权限管理器（条件装配：需要DynamicSecurityService Bean时才创建）
     */
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean
    public DynamicAuthorizationManager dynamicAuthorizationManager(DynamicSecurityMetadataSource securityDataSource, IgnoreUrlsConfig ignoreUrlsConfig) {
        return new DynamicAuthorizationManager(securityDataSource, ignoreUrlsConfig);
    }
}
