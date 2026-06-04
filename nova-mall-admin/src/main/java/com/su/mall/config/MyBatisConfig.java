package com.su.mall.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus相关配置
 * @author Su
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.su.mall.mapper", "com.su.mall.dao"})
public class MyBatisConfig {

    /**
     * 配置 MyBatis-Plus 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加 MySQL 分页拦截器
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(500L); // 单页最多500条，防刷
        paginationInnerInterceptor.setOverflow(true);  // 页码超出总页返回第一页
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}