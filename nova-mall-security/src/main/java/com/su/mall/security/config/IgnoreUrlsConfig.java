package com.su.mall.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security白名单资源路径配置
 * <p>配置前缀：secure.ignored.urls
 * <p>白名单中的URL无需认证即可访问（如登录、注册、首页等公开接口）
 * <p>配置示例：secure.ignored.urls[0]=/admin/login
 *
 * @see SecurityConfig#filterChain
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "secure.ignored")
public class IgnoreUrlsConfig {

    private List<String> urls = new ArrayList<>();

}
