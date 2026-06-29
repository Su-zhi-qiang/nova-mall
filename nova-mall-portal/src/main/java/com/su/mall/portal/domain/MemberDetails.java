package com.su.mall.portal.domain;

import com.su.mall.model.UmsMember;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

/**
 * Spring Security用户详情封装
 * <p>将 {@link UmsMember} 适配为Spring Security的UserDetails接口
 * <p>用于认证流程中的用户身份加载和权限校验
 *
 * @see com.su.mall.portal.config.MallSecurityConfig
 */
public class MemberDetails implements UserDetails {

    private final UmsMember umsMember;

    public MemberDetails(UmsMember umsMember) {
        this.umsMember = umsMember;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("TEST"));
    }

    @Override
    public String getPassword() {
        return umsMember.getPassword();
    }

    @Override
    public String getUsername() {
        return umsMember.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return umsMember.getStatus() == 1;
    }

    /**
     * 获取原始会员对象
     *
     * @return UmsMember实体
     */
    public UmsMember getUmsMember() {
        return umsMember;
    }
}
