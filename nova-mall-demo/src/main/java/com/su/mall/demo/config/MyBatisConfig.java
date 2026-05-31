package com.su.mall.demo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis相关配置
 * @author Su
 */
@Configuration
@MapperScan("com.su.mall.mapper")
public class MyBatisConfig {
}
