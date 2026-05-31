package com.su.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis相关配置
 * @author Su
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.su.mall.mapper","com.su.mall.dao"})
public class MyBatisConfig {
}
