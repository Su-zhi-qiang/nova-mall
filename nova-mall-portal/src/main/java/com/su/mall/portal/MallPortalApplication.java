package com.su.mall.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mall前台系统启动类
 * <p>扫描 com.su.mall 包下的所有组件（包括 model、mapper 等公共模块）
 * <p>默认端口：8085
 */
@SpringBootApplication(scanBasePackages = "com.su.mall")
public class MallPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPortalApplication.class, args);
    }
}
