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

@Configuration
@RequiredArgsConstructor
public class MallSecurityConfig {

    private final UmsAdminService adminService;
    private final UmsResourceService resourceService;

    @Bean
    public UserDetailsService userDetailsService() {
        return adminService::loadUserByUsername;
    }

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