package com.su.mall.search.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis相关配置
 * @author Su
 */
@Configuration
@MapperScan({"com.su.mall.mapper","com.su.mall.search.dao"})
public class MyBatisConfig {
}
