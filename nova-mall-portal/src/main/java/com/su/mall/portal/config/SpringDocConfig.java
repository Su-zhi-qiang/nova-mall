package com.su.mall.portal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * SpringDoc（Swagger3）接口文档配置
 * <p>配置API文档标题、描述、服务器地址和JWT认证方案
 * <p>访问地址：http://localhost:8085/swagger-ui/index.html
 */
@Configuration
public class SpringDocConfig implements WebMvcConfigurer {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Value("${server.port:8085}")
    private String serverPort;

    /**
     * 配置OpenAPI文档元信息
     * <p>包含标题、版本、联系方式、许可证和JWT认证方案
     */
    @Bean
    public OpenAPI mallPortalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("mall前台系统")
                        .description("mall前台相关接口文档")
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
     * 将 /swagger-ui/ 重定向到 /swagger-ui/index.html
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui/").setViewName("redirect:/swagger-ui/index.html");
    }
}
