package com.su.mall.config;

import com.su.mall.model.UmsResource;
import com.su.mall.security.component.CustomConfigAttribute;
import com.su.mall.security.component.DynamicSecurityService;
import com.su.mall.service.UmsAdminService;
import com.su.mall.service.UmsResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mall安全配置（Admin模块）
 * <p>将Admin模块的业务Service注入到通用安全框架中：
 * <ul>
 *   <li>{@link #userDetailsService()} — 提供基于用户名的用户加载能力，供JWT过滤器校验身份</li>
 *   <li>{@link #dynamicSecurityService()} — 提供URL→权限编码的映射，供动态权限管理器做访问控制</li>
 * </ul>
 * <p>此配置被 {@link SecurityConfig} 中的 {@code dynamicAuthorizationManager} 间接消费
 *
 * @see UmsAdminService#loadUserByUsername(String)
 * @see UmsResourceService#listAll()
 */
@Configuration
@RequiredArgsConstructor
public class MallSecurityConfig {

    private final UmsAdminService adminService;
    private final UmsResourceService resourceService;

    /**
     * 注册UserDetailsService，供Spring Security的认证流程使用
     * <p>JWT过滤器调用 loadUserByUsername 从数据库加载用户详情，
     * 用于验证Token有效性并构建SecurityContext中的Authentication对象
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return adminService::loadUserByUsername;
    }

    /**
     * 注册动态安全数据源，提供URL→权限编码的运行时映射
     * <p>每次权限校验时从数据库全量加载所有资源记录，
     * 以ConcurrentHashMap存储，key为资源URL，value为资源编码+名称
     *
     * @see DynamicSecurityMetadataSource
     * @see DynamicAuthorizationManager
     */
    @Bean
    public DynamicSecurityService dynamicSecurityService() {
        return () -> {
            Map<String, CustomConfigAttribute> map = new ConcurrentHashMap<>();
            List<UmsResource> resourceList = resourceService.listAll();
            for (UmsResource resource : resourceList) {
                map.put(resource.getUrl(), new CustomConfigAttribute(resource.getId() + ":" + resource.getName()));
            }
            return map;
        };
    }
}