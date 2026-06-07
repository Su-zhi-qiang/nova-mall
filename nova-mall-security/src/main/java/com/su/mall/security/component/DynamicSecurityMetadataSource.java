package com.su.mall.security.component;

import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import jakarta.annotation.PostConstruct;
import java.util.*;

@RequiredArgsConstructor
public class DynamicSecurityMetadataSource {

    private static Map<String, CustomConfigAttribute> configAttributeMap = null;
    private final DynamicSecurityService dynamicSecurityService;

    @PostConstruct
    public void loadDataSource() {
        configAttributeMap = dynamicSecurityService.loadDataSource();
    }

    public void clearDataSource() {
        configAttributeMap.clear();
        configAttributeMap = null;
    }

    public List<CustomConfigAttribute> getConfigAttributesWithPath(String path) {
        if (configAttributeMap == null) {
            this.loadDataSource();
        }
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