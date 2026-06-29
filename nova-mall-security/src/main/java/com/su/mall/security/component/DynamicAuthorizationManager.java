package com.su.mall.security.component;

import cn.hutool.core.collection.CollUtil;
import com.su.mall.security.config.IgnoreUrlsConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 动态URL权限管理器
 * <p>运行时从数据库加载URL-角色映射，动态判断当前用户是否有权限访问
 * <p>判断流程：白名单直接放行 → OPTIONS放行 → 查找URL所需权限 → 比对用户已有权限
 *
 * @see DynamicSecurityMetadataSource 权限元数据源
 * @see IgnoreUrlsConfig 白名单配置
 */
@RequiredArgsConstructor
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final DynamicSecurityMetadataSource securityDataSource;
    private final IgnoreUrlsConfig ignoreUrlsConfig;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext requestAuthorizationContext) {
        // 1. 获取当前请求的URI路径
        HttpServletRequest request = requestAuthorizationContext.getRequest();
        String path = request.getRequestURI();
        PathMatcher pathMatcher = new AntPathMatcher();

        // 2. 白名单路径直接放行
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, path)) {
                return new AuthorizationDecision(true);
            }
        }

        // 3. OPTIONS预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return new AuthorizationDecision(true);
        }

        // 4. 从元数据源获取当前URL所需的角色权限列表
        List<CustomConfigAttribute> configAttributeList = securityDataSource.getConfigAttributesWithPath(path);

        // 5. 该URL未配置任何权限要求，已认证用户即可访问
        if (CollUtil.isEmpty(configAttributeList)) {
            Authentication currentAuth = authentication.get();
            return new AuthorizationDecision(currentAuth.isAuthenticated());
        }

        // 6. 提取所需权限编码列表
        List<String> needAuthorities = configAttributeList.stream()
                .map(CustomConfigAttribute::attribute)
                .toList();

        // 7. 比对用户已有权限和所需权限
        Authentication currentAuth = authentication.get();
        if (currentAuth.isAuthenticated()) {
            Collection<? extends GrantedAuthority> grantedAuthorities = currentAuth.getAuthorities();

            // 8. 筛选出用户拥有的、且符合URL要求的权限
            List<? extends GrantedAuthority> hasAuth = grantedAuthorities.stream()
                    .filter(item -> needAuthorities.contains(item.getAuthority()))
                    .collect(Collectors.toList());

            // 9. 有匹配权限则放行，否则拒绝
            if (CollUtil.isNotEmpty(hasAuth)) {
                return new AuthorizationDecision(true);
            } else {
                return new AuthorizationDecision(false);
            }
        } else {
            // 10. 未认证，拒绝访问
            return new AuthorizationDecision(false);
        }
    }
}
