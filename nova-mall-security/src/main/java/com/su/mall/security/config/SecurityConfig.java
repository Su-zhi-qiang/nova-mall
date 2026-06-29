package com.su.mall.security.config;

import com.su.mall.security.component.DynamicAuthorizationManager;
import com.su.mall.security.component.JwtAuthenticationTokenFilter;
import com.su.mall.security.component.RestAuthenticationEntryPoint;
import com.su.mall.security.component.RestfulAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Spring Security安全配置
 * <p>配置认证授权链：白名单放行 → CSRF禁用 → 无状态Session → JWT过滤器 → 动态权限管理
 *
 * @see JwtAuthenticationTokenFilter JWT认证过滤器
 * @see DynamicAuthorizationManager 动态权限管理器
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final IgnoreUrlsConfig ignoreUrlsConfig;
    private final RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired(required = false)
    private DynamicAuthorizationManager dynamicAuthorizationManager;

    /**
     * 配置Security过滤器链
     * <p>配置顺序：白名单 → OPTIONS放行 → 动态权限/认证要求 → CSRF禁用 → 无状态Session → 异常处理 → JWT过滤器
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(registry -> {
            // 1. 白名单路径直接放行（登录、注册、首页等公开接口）
            for (String url : ignoreUrlsConfig.getUrls()) {
                registry.requestMatchers(url).permitAll();
            }
            // 2. 跨域预检请求OPTIONS直接放行
            registry.requestMatchers(HttpMethod.OPTIONS).permitAll();
        })
        // 3. 其他请求需要认证，有动态权限配置时使用动态权限管理器
        .authorizeHttpRequests(registry -> registry.anyRequest()
            .access(dynamicAuthorizationManager == null
                    ? AuthenticatedAuthorizationManager.authenticated()  // 无动态权限：只要已认证即可
                    : dynamicAuthorizationManager)                       // 有动态权限：按URL-角色映射判断
        )
        // 4. 禁用CSRF（REST API不需要）
        .csrf(AbstractHttpConfigurer::disable)
        // 5. 设置Session策略为无状态（JWT不需要Session）
        .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 6. 配置权限不足和未认证的异常处理（返回JSON而非重定向）
        .exceptionHandling(configurer -> configurer
                .accessDeniedHandler(restfulAccessDeniedHandler)
                .authenticationEntryPoint(restAuthenticationEntryPoint))
        // 7. 在UsernamePasswordAuthenticationFilter之前插入JWT过滤器
        .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
