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
 * 动态权限验证管理器，根据资源配置动态判断用户是否有访问权限
 * @author Su
 */
@RequiredArgsConstructor
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final DynamicSecurityMetadataSource securityDataSource;
    private final IgnoreUrlsConfig ignoreUrlsConfig;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext requestAuthorizationContext) {
        HttpServletRequest request = requestAuthorizationContext.getRequest();
        String path = request.getRequestURI();
        PathMatcher pathMatcher = new AntPathMatcher();
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, path)) {
                return new AuthorizationDecision(true);
            }
        }
        if ("OPTIONS".equals(request.getMethod())) {
            return new AuthorizationDecision(true);
        }
        List<CustomConfigAttribute> configAttributeList = securityDataSource.getConfigAttributesWithPath(path);
        // 如果该路径没有配置任何资源权限，则已认证用户可访问
        if (CollUtil.isEmpty(configAttributeList)) {
            Authentication currentAuth = authentication.get();
            return new AuthorizationDecision(currentAuth.isAuthenticated());
        }
        List<String> needAuthorities = configAttributeList.stream()
                .map(CustomConfigAttribute::attribute)
                .toList();
        Authentication currentAuth = authentication.get();
        if (currentAuth.isAuthenticated()) {
            Collection<? extends GrantedAuthority> grantedAuthorities = currentAuth.getAuthorities();
            List<? extends GrantedAuthority> hasAuth = grantedAuthorities.stream()
                    .filter(item -> needAuthorities.contains(item.getAuthority()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(hasAuth)) {
                return new AuthorizationDecision(true);
            } else {
                return new AuthorizationDecision(false);
            }
        } else {
            return new AuthorizationDecision(false);
        }
    }
}
