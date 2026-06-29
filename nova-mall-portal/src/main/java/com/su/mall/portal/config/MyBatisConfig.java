package com.su.mall.portal.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus配置
 * <p>启用事务管理，扫描Mapper接口所在包
 * <p>配置MySQL分页插件，支持物理分页
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.su.mall.mapper", "com.su.mall.portal.dao"})
public class MyBatisConfig {

    /**
     * 注册MyBatis-Plus分页拦截器
     * <p>使用MySQL方言进行物理分页
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
