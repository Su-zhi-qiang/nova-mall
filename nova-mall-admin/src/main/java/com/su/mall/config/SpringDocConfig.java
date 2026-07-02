package com.su.mall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * SpringDoc (OpenAPI 3.0) 配置
 * <p>定义后台管理系统的Swagger文档元信息（标题、描述、版本、联系方式）
 * <p>配置JWT Bearer认证方案，使Swagger UI中的接口可直接携带Token调试
 * <p>同时注册视图控制器，将 /swagger-ui/ 重定向到 /swagger-ui/index.html
 */
@Configuration
public class SpringDocConfig implements WebMvcConfigurer {

    /** 安全方案名称（与Swagger UI中的Authorize按钮对应） */
    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 配置OpenAPI文档元信息
     * <p>包含文档标题、描述、版本、服务器地址、JWT安全方案
     */
    @Bean
    public OpenAPI mallAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("mall后台系统")
                        .description("mall后台相关接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("技术支持")
                                .email("support@mall.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地开发环境")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT令牌认证")));
    }

    /**
     * 自定义 OpenAPI 扩展配置
     */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            // 设置文档排序规则
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    // 可以在这里对路径进行自定义处理
                });
            }
        };
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //配置访问`/swagger-ui/`路径时可以直接跳转到`/swagger-ui/index.html`
        registry.addViewController("/swagger-ui/").setViewName("redirect:/swagger-ui/index.html");
    }

}

