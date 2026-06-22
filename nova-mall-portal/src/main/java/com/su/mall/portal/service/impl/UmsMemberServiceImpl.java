package com.su.mall.portal.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.common.exception.Asserts;
import com.su.mall.mapper.UmsMemberLevelMapper;
import com.su.mall.mapper.UmsMemberMapper;
import com.su.mall.model.UmsMember;
import com.su.mall.model.UmsMemberLevel;
import com.su.mall.portal.domain.MemberDetails;
import com.su.mall.portal.service.UmsMemberCacheService;
import com.su.mall.portal.service.UmsMemberService;
import com.su.mall.security.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 会员管理Service实现类
 *
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsMemberServiceImpl implements UmsMemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsMemberServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final UmsMemberMapper memberMapper;
    private final UmsMemberLevelMapper memberLevelMapper;
    private final UmsMemberCacheService memberCacheService;
    @Value("${redis.key.authCode}")
    private String REDIS_KEY_PREFIX_AUTH_CODE;
    @Value("${redis.expire.authCode}")
    private Long AUTH_CODE_EXPIRE_SECONDS;

    @Override
    public UmsMember getByUsername(String username) {
        UmsMember member = memberCacheService.getMember(username);
        if(member!=null) return member;
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<UmsMember>())
        List<UmsMember> memberList = memberMapper.selectList(
                new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getUsername, username));
        if (!CollectionUtils.isEmpty(memberList)) {
            member = memberList.get(0);
            memberCacheService.setMember(member);
            return member;
        }
        return null;
    }

    @Override
    public UmsMember getById(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return memberMapper.selectById(id);
    }

    @Override
    @Transactional
    public void register(String username, String password, String telephone, String authCode) {
        //验证验证码
        if(!verifyAuthCode(authCode,telephone)){
            Asserts.fail("验证码错误");
        }
        //查询是否已有该用户
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<UmsMember>())
        List<UmsMember> umsMembers = memberMapper.selectList(
                new LambdaQueryWrapper<UmsMember>()
                        .eq(UmsMember::getUsername, username)
                        .or()
                        .eq(UmsMember::getPhone, telephone));
        if (!CollectionUtils.isEmpty(umsMembers)) {
            Asserts.fail("该用户已经存在");
        }
        //没有该用户进行添加操作
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPhone(telephone);
        umsMember.setPassword(passwordEncoder.encode(password));
        umsMember.setCreateTime(new Date());
        umsMember.setStatus(1);
        //获取默认会员等级并设置
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<UmsMemberLevel>())
        List<UmsMemberLevel> memberLevelList = memberLevelMapper.selectList(
                new LambdaQueryWrapper<UmsMemberLevel>().eq(UmsMemberLevel::getDefaultStatus, 1));
        if (!CollectionUtils.isEmpty(memberLevelList)) {
            umsMember.setMemberLevelId(memberLevelList.get(0).getId());
        }
        // ✅ 改造：insertSelective → insert
        memberMapper.insert(umsMember);
        umsMember.setPassword(null);
        memberCacheService.setMember(umsMember);
    }

    @Override
    public String generateAuthCode(String telephone) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i=0;i<6;i++){
            sb.append(random.nextInt(10));
        }
        memberCacheService.setAuthCode(telephone,sb.toString());
        return sb.toString();
    }

    @Override
    @Transactional
    public void updatePassword(String telephone, String password, String authCode) {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<UmsMember>())
        List<UmsMember> memberList = memberMapper.selectList(
                new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getPhone, telephone));
        if(CollectionUtils.isEmpty(memberList)){
            Asserts.fail("该账号不存在");
        }
        //验证验证码
        if(!verifyAuthCode(authCode,telephone)){
            Asserts.fail("验证码错误");
        }
        UmsMember umsMember = memberList.get(0);
        umsMember.setPassword(passwordEncoder.encode(password));
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        memberMapper.updateById(umsMember);
        memberCacheService.delMember(umsMember.getId());
    }

    @Override
    public UmsMember getCurrentMember() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = ctx.getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof MemberDetails memberDetails)) {
            return null;
        }
        return memberDetails.getUmsMember();
    }

    @Override
    @Transactional
    public void updateIntegration(Long id, Integer integration) {
        UmsMember record=new UmsMember();
        record.setId(id);
        record.setIntegration(integration);
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        memberMapper.updateById(record);
        memberCacheService.delMember(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UmsMember member = getByUsername(username);
        if(member!=null){
            return new MemberDetails(member);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    @Override
    public String login(String username, String password) {
        String token = null;
        //密码需要客户端加密后传递
        try {
            UserDetails userDetails = loadUserByUsername(username);
            if(!passwordEncoder.matches(password,userDetails.getPassword())){
                throw new BadCredentialsException("密码不正确");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }

    @Override
    public String refreshToken(String token) {
        return jwtTokenUtil.refreshHeadToken(token);
    }

    //对输入的验证码进行校验
    private boolean verifyAuthCode(String authCode, String telephone){
        if(StrUtil.isEmpty(authCode)){
            return false;
        }
        String realAuthCode = memberCacheService.getAuthCode(telephone);
        return authCode.equals(realAuthCode);
    }

}
