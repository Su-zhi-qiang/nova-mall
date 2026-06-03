package com.su.mall.config;

import com.github.pagehelper.PageInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

/**
 * MyBatis-Plus相关配置
 * @author Su
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.su.mall.mapper", "com.su.mall.dao"})
public class MyBatisConfig {

    /**
     * 配置 PageHelper 分页插件
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        // 设置数据库类型为 MySQL
        properties.setProperty("helperDialect", "mysql");
        // 合理化分页参数（页码小于1时返回第一页，页码大于总页数时返回最后一页）
        properties.setProperty("reasonable", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }
}