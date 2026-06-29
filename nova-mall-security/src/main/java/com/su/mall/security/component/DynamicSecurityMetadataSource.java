package com.su.mall.security.component;

import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 动态安全元数据源
 * <p>应用启动时从数据库加载URL-权限映射关系，缓存到内存Map中
 * <p>供 {@link DynamicAuthorizationManager} 在运行时查询URL所需的权限配置
 *
 * @see DynamicSecurityService 从数据库加载资源的服务接口
 */
@RequiredArgsConstructor
public class DynamicSecurityMetadataSource {

    /** URL模式 → 权限配置的映射缓存 */
    private static Map<String, CustomConfigAttribute> configAttributeMap = null;

    private final DynamicSecurityService dynamicSecurityService;

    /**
     * 应用启动后自动加载权限数据到内存
     */
    @PostConstruct
    public void loadDataSource() {
        configAttributeMap = dynamicSecurityService.loadDataSource();
    }

    /**
     * 清空权限缓存（用于权限变更后刷新）
     */
    public void clearDataSource() {
        configAttributeMap.clear();
        configAttributeMap = null;
    }

    /**
     * 根据请求路径获取所需的权限配置
     * <p>使用AntPathMatcher进行路径模式匹配（如 /api/** 匹配 /api/user/list）
     *
     * @param path 当前请求的URI路径
     * @return 匹配到的权限配置列表
     */
    public List<CustomConfigAttribute> getConfigAttributesWithPath(String path) {
        // 1. 如果缓存为空，重新加载
        if (configAttributeMap == null) {
            this.loadDataSource();
        }

        // 2. 遍历所有URL模式，匹配当前路径
        List<CustomConfigAttribute> configAttributes = new ArrayList<>();
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String pattern : configAttributeMap.keySet()) {
            if (pathMatcher.match(pattern, path)) {
                configAttributes.add(configAttributeMap.get(pattern));
            }
        }

        return configAttributes;
    }
}
