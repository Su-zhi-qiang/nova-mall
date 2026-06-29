package com.su.mall.portal.service.impl;

import com.su.mall.common.service.RedisService;
import com.su.mall.mapper.UmsMemberMapper;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.service.UmsMemberCacheService;
import com.su.mall.security.annotation.CacheException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 会员缓存Service实现
 * <p>负责会员信息和验证码的Redis缓存读写
 * <p>异常隔离策略：
 * <ul>
 *   <li>标注 @CacheException 的方法（getAuthCode/setAuthCode/getMember）：Redis异常时抛出，保证验证码等关键业务可靠</li>
 *   <li>未标注的方法（setMember/delMember）：Redis异常时静默失败，不影响主业务流程</li>
 * </ul>
 *
 * @see com.su.mall.security.aspect.RedisCacheAspect 异常隔离切面
 * @see com.su.mall.security.annotation.CacheException 异常隔离注解
 */
@Service
@RequiredArgsConstructor
public class UmsMemberCacheServiceImpl implements UmsMemberCacheService {
    private final RedisService redisService;
    private final UmsMemberMapper memberMapper;

    @Value("${redis.database}")
    private String REDIS_DATABASE;           // Redis数据库前缀

    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;               // 会员缓存过期时间（秒）

    @Value("${redis.expire.authCode}")
    private Long REDIS_EXPIRE_AUTH_CODE;     // 验证码过期时间（秒）

    @Value("${redis.key.member}")
    private String REDIS_KEY_MEMBER;         // 会员缓存key前缀

    @Value("${redis.key.authCode}")
    private String REDIS_KEY_AUTH_CODE;      // 验证码缓存key前缀

    /**
     * 删除会员缓存（用于修改会员信息后刷新缓存）
     */
    @Override
    public void delMember(Long memberId) {
        // 1. 根据会员ID查询用户名，构建缓存key
        UmsMember umsMember = memberMapper.selectById(memberId);
        if (umsMember != null) {
            String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + umsMember.getUsername();
            // 2. 删除缓存
            redisService.del(key);
        }
    }

    /**
     * 获取会员缓存（标注 @CacheException，Redis异常时抛出）
     * <p>供 loadUserByUsername() 调用，Redis宕机时需感知以走DB降级
     */
    @CacheException
    @Override
    public UmsMember getMember(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + username;
        return redisService.get(key);
    }

    /**
     * 设置会员缓存
     */
    @Override
    public void setMember(UmsMember member) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + member.getUsername();
        redisService.set(key, member, REDIS_EXPIRE);
    }

    /**
     * 设置验证码（标注 @CacheException，Redis异常时抛出）
     * <p>验证码写入必须成功，否则用户无法完成注册/登录
     */
    @CacheException
    @Override
    public void setAuthCode(String telephone, String authCode) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        redisService.set(key, authCode, REDIS_EXPIRE_AUTH_CODE);
    }

    /**
     * 获取验证码（标注 @CacheException，Redis异常时抛出）
     * <p>验证码读取必须成功，否则无法校验用户输入
     */
    @CacheException
    @Override
    public String getAuthCode(String telephone) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        return redisService.get(key);
    }
}
