package com.su.mall.security.component;

import java.util.Map;

/**
 * 动态安全服务接口，用于获取资源列表
 * @author Su
 */
public interface DynamicSecurityService {
    Map<String, CustomConfigAttribute> loadDataSource();
}