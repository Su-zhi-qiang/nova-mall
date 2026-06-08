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
 * UmsMemberCacheService实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsMemberCacheServiceImpl implements UmsMemberCacheService {
    private final RedisService redisService;
    private final UmsMemberMapper memberMapper;
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    @Value("${redis.expire.authCode}")
    private Long REDIS_EXPIRE_AUTH_CODE;
    @Value("${redis.key.member}")
    private String REDIS_KEY_MEMBER;
    @Value("${redis.key.authCode}")
    private String REDIS_KEY_AUTH_CODE;

    @Override
    public void delMember(Long memberId) {
        // ✅ 改造：selectByPrimaryKey → selectById
        UmsMember umsMember = memberMapper.selectById(memberId);
        if (umsMember != null) {
            String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + umsMember.getUsername();
            redisService.del(key);
        }
    }

    @CacheException
    @Override
    public UmsMember getMember(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + username;
        return redisService.get(key);
    }

    @Override
    public void setMember(UmsMember member) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + member.getUsername();
        redisService.set(key, member, REDIS_EXPIRE);
    }
    
    @CacheException
    @Override
    public void setAuthCode(String telephone, String authCode) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        redisService.set(key,authCode,REDIS_EXPIRE_AUTH_CODE);
    }

    @CacheException
    @Override
    public String getAuthCode(String telephone) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        return redisService.get(key);
    }
}