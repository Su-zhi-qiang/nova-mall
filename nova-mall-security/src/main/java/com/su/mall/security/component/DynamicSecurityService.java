package com.su.mall.security.component;

import java.util.Map;

public interface DynamicSecurityService {
    Map<String, CustomConfigAttribute> loadDataSource();
}