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
 * 会员管理Service实现
 * <p>核心功能：注册、登录、Token生成/续期、验证码生成/校验、积分管理
 * <p>缓存策略：查询先走Redis缓存，miss后查DB并回写缓存；修改/删除时清除缓存
 *
 * @see UmsMemberCacheService 会员缓存服务
 * @see JwtTokenUtil JWT工具
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

    /**
     * 根据用户名获取会员（缓存优先）
     * <p>查询顺序：Redis缓存 → DB → 回写缓存
     */
    @Override
    public UmsMember getByUsername(String username) {
        // 1. 先从Redis缓存获取
        UmsMember member = memberCacheService.getMember(username);
        if (member != null) return member;

        // 2. 缓存未命中，查询数据库
        List<UmsMember> memberList = memberMapper.selectList(
                new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getUsername, username));
        if (!CollectionUtils.isEmpty(memberList)) {
            member = memberList.get(0);
            // 3. 回写缓存
            memberCacheService.setMember(member);
            return member;
        }
        return null;
    }

    @Override
    public UmsMember getById(Long id) {
        return memberMapper.selectById(id);
    }

    /**
     * 会员注册
     * <p>流程：校验验证码 → 检查用户名/手机号是否重复 → 创建会员 → 设置默认等级 → 入库 → 写缓存
     */
    @Override
    @Transactional
    public void register(String username, String password, String telephone, String authCode) {
        // 1. 验证验证码是否正确
        if (!verifyAuthCode(authCode, telephone)) {
            Asserts.fail("验证码错误");
        }

        // 2. 检查用户名或手机号是否已存在
        List<UmsMember> umsMembers = memberMapper.selectList(
                new LambdaQueryWrapper<UmsMember>()
                        .eq(UmsMember::getUsername, username)
                        .or()
                        .eq(UmsMember::getPhone, telephone));
        if (!CollectionUtils.isEmpty(umsMembers)) {
            Asserts.fail("该用户已经存在");
        }

        // 3. 构建新会员对象
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPhone(telephone);
        umsMember.setPassword(passwordEncoder.encode(password)); // 密码BCrypt加密
        umsMember.setCreateTime(new Date());
        umsMember.setStatus(1); // 状态：正常

        // 4. 设置默认会员等级
        List<UmsMemberLevel> memberLevelList = memberLevelMapper.selectList(
                new LambdaQueryWrapper<UmsMemberLevel>().eq(UmsMemberLevel::getDefaultStatus, 1));
        if (!CollectionUtils.isEmpty(memberLevelList)) {
            umsMember.setMemberLevelId(memberLevelList.get(0).getId());
        }

        // 5. 插入数据库
        memberMapper.insert(umsMember);

        // 6. 写入缓存（清除密码后缓存）
        umsMember.setPassword(null);
        memberCacheService.setMember(umsMember);
    }

    /**
     * 生成6位随机验证码并存入Redis
     *
     * @param telephone 手机号
     * @return 6位验证码
     */
    @Override
    public String generateAuthCode(String telephone) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        // 验证码存入Redis（标注@CacheException，Redis异常时会抛出）
        memberCacheService.setAuthCode(telephone, sb.toString());
        return sb.toString();
    }

    /**
     * 修改密码
     * <p>流程：校验账号存在 → 验证验证码 → 加密新密码 → 更新DB → 清除缓存
     */
    @Override
    @Transactional
    public void updatePassword(String telephone, String password, String authCode) {
        // 1. 根据手机号查找会员
        List<UmsMember> memberList = memberMapper.selectList(
                new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getPhone, telephone));
        if (CollectionUtils.isEmpty(memberList)) {
            Asserts.fail("该账号不存在");
        }

        // 2. 验证验证码
        if (!verifyAuthCode(authCode, telephone)) {
            Asserts.fail("验证码错误");
        }

        // 3. 加密新密码并更新
        UmsMember umsMember = memberList.get(0);
        umsMember.setPassword(passwordEncoder.encode(password));
        memberMapper.updateById(umsMember);

        // 4. 清除会员缓存（下次登录会重新加载）
        memberCacheService.delMember(umsMember.getId());
    }

    /**
     * 获取当前登录会员
     * <p>从SecurityContext中提取认证信息，返回会员实体
     */
    @Override
    public UmsMember getCurrentMember() {
        // 1. 获取Security上下文
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = ctx.getAuthentication();
        if (auth == null) {
            return null;
        }

        // 2. 提取认证主体（MemberDetails）
        Object principal = auth.getPrincipal();
        if (!(principal instanceof MemberDetails memberDetails)) {
            return null;
        }

        // 3. 返回原始会员对象
        return memberDetails.getUmsMember();
    }

    /**
     * 更新会员积分
     * <p>更新DB后清除缓存，确保下次读取最新积分
     */
    @Override
    @Transactional
    public void updateIntegration(Long id, Integer integration) {
        UmsMember record = new UmsMember();
        record.setId(id);
        record.setIntegration(integration);
        memberMapper.updateById(record);

        // 清除缓存
        memberCacheService.delMember(id);
    }

    /**
     * Spring Security加载用户详情（供认证流程调用）
     * <p>根据用户名查询会员，存在则包装为MemberDetails返回
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        UmsMember member = getByUsername(username);
        if (member != null) {
            return new MemberDetails(member);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    /**
     * 会员登录
     * <p>流程：加载用户 → 校验密码 → 设置SecurityContext → 生成JWT Token
     *
     * @param username 用户名
     * @param password 密码
     * @return JWT Token，登录失败返回null
     */
    @Override
    public String login(String username, String password) {
        String token = null;
        try {
            // 1. 加载用户详情
            UserDetails userDetails = loadUserByUsername(username);

            // 2. 校验密码是否匹配
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new BadCredentialsException("密码不正确");
            }

            // 3. 创建认证对象并设置到SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. 生成JWT Token
            token = jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }

    /**
     * Token自动续期
     * <p>委托给 {@link JwtTokenUtil#refreshHeadToken} 处理
     *
     * @param token 带前缀的旧Token
     * @return 续期后的新Token，无法续期返回null
     */
    @Override
    public String refreshToken(String token) {
        return jwtTokenUtil.refreshHeadToken(token);
    }

    /**
     * 校验验证码
     * <p>从Redis获取存储的验证码，与用户输入进行比较
     *
     * @param authCode  用户输入的验证码
     * @param telephone 手机号
     * @return true=验证码正确
     */
    private boolean verifyAuthCode(String authCode, String telephone) {
        if (StrUtil.isEmpty(authCode)) {
            return false;
        }
        String realAuthCode = memberCacheService.getAuthCode(telephone);
        return authCode.equals(realAuthCode);
    }
}
