package com.su.mall.portal.config;

import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * mall前台安全模块配置
 * <p>注册UserDetailsService，供Spring Security认证流程使用
 * <p>通过 {@link UmsMemberService#loadUserByUsername} 加载会员信息
 *
 * @see com.su.mall.portal.domain.MemberDetails
 */
@Configuration
@RequiredArgsConstructor
public class MallSecurityConfig {

    private final UmsMemberService memberService;

    /**
     * 注册用户详情服务
     * <p>根据用户名从数据库/缓存加载会员信息，用于登录认证
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> memberService.loadUserByUsername(username);
    }
}
