package com.su.mall.security.component;

import cn.hutool.core.collection.CollUtil;
import com.su.mall.security.config.IgnoreUrlsConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
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

@RequiredArgsConstructor
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final DynamicSecurityMetadataSource securityDataSource;
    private final IgnoreUrlsConfig ignoreUrlsConfig;

    @SuppressWarnings("deprecation")
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
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            return new AuthorizationDecision(true);
        }
        List<CustomConfigAttribute> configAttributeList = securityDataSource.getConfigAttributesWithPath(path);
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
