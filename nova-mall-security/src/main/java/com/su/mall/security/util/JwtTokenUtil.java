package com.su.mall.security.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token工具类
 * <p>基于Hutool JWTUtil实现，提供Token的生成、验证、解析和自动续期
 * <p>Token格式：header.payload.signature
 * <p>payload包含：sub(用户名)、created(创建时间)、exp(过期时间)
 *
 * @see com.su.mall.security.component.JwtAuthenticationTokenFilter
 */
public class JwtTokenUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);
    private static final String CLAIM_KEY_USERNAME = "sub";      // payload中的用户名字段
    private static final String CLAIM_KEY_CREATED = "created";  // payload中的创建时间字段

    @Value("${jwt.secret}")
    private String secret;        // 签名密钥

    @Value("${jwt.expiration}")
    private Long expiration;      // 过期时间（秒）

    @Value("${jwt.tokenHead}")
    private String tokenHead;     // Token前缀（如 "Bearer "）

    /**
     * 获取签名密钥字节数组
     */
    private byte[] getSigningKey() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 根据claims生成JWT Token
     * <p>自动设置过期时间 = 当前时间 + expiration秒
     *
     * @param claims 负载数据（用户名、创建时间等）
     * @return JWT Token字符串
     */
    private String generateToken(Map<String, Object> claims) {
        // 1. 计算过期时间戳
        long expireTime = System.currentTimeMillis() + expiration * 1000;
        claims.put("exp", expireTime);

        // 2. 使用Hutool JWTUtil生成Token
        return JWTUtil.createToken(claims, getSigningKey());
    }

    /**
     * 从Token中解析payload
     * <p>先验证签名，签名无效则返回null
     *
     * @param token JWT Token字符串
     * @return payload数据Map，签名无效或格式错误返回null
     */
    private Map<String, Object> getPayloadFromToken(String token) {
        try {
            // 1. 验证Token签名
            if (!JWTUtil.verify(token, getSigningKey())) {
                LOGGER.info("JWT签名验证失败:{}", token);
                return null;
            }
            // 2. 解析Token payload
            return JWTUtil.parseToken(token).getPayloads();
        } catch (Exception e) {
            LOGGER.info("JWT格式验证失败:{}", token);
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名，解析失败返回null
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Map<String, Object> payload = getPayloadFromToken(token);
            username = payload != null ? (String) payload.get(CLAIM_KEY_USERNAME) : null;
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 验证Token是否有效
     * <p>校验条件：用户名匹配 + Token未过期
     *
     * @param token       客户端传入的Token
     * @param userDetails 从数据库查询的用户信息
     * @return true=Token有效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);
        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * 判断Token是否已过期
     * <p>通过比较payload中的exp字段和当前时间戳
     */
    private boolean isTokenExpired(String token) {
        try {
            Map<String, Object> payload = getPayloadFromToken(token);
            if (payload == null) {
                return true;
            }
            Object exp = payload.get("exp");
            if (exp == null) {
                return false;
            }
            long expTime = exp instanceof Long ? (Long) exp : ((Number) exp).longValue();
            return expTime < System.currentTimeMillis();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从Token中获取过期时间
     */
    private Date getExpiredDateFromToken(String token) {
        Map<String, Object> payload = getPayloadFromToken(token);
        if (payload == null) {
            return null;
        }
        Object exp = payload.get("exp");
        if (exp instanceof Long) {
            return new Date((Long) exp);
        } else if (exp instanceof Integer) {
            return new Date(((Integer) exp).longValue());
        }
        return null;
    }

    /**
     * 根据用户信息生成Token
     *
     * @param userDetails Spring Security用户详情
     * @return JWT Token字符串
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * Token自动续期（核心方法）
     * <p>处理流程：
     * <ol>
     *   <li>截取Token前缀（Bearer ）</li>
     *   <li>校验签名是否有效</li>
     *   <li>Token已过期 → 返回null（需重新登录）</li>
     *   <li>Token在30分钟内刚刷新过 → 返回原Token（避免频繁刷新）</li>
     *   <li>否则 → 更新created时间，生成新Token返回</li>
     * </ol>
     *
     * @param oldToken 带tokenHead前缀的完整Token
     * @return 续期后的新Token，无法续期返回null
     */
    public String refreshHeadToken(String oldToken) {
        // 1. 空值校验
        if (StrUtil.isEmpty(oldToken)) {
            return null;
        }

        // 2. 截取Token前缀（如 "Bearer "）
        String token = oldToken.substring(tokenHead.length());
        if (StrUtil.isEmpty(token)) {
            return null;
        }

        // 3. 验证Token签名
        Map<String, Object> payload = getPayloadFromToken(token);
        if (payload == null) {
            return null;
        }

        // 4. Token已过期，不支持刷新（需重新登录）
        if (isTokenExpired(token)) {
            return null;
        }

        // 5. 30分钟内刚刷新过，返回原Token（避免频繁刷新）
        if (tokenRefreshJustBefore(token)) {
            return token;
        } else {
            // 6. 更新创建时间，生成新Token
            payload.put(CLAIM_KEY_CREATED, new Date());
            return generateToken(payload);
        }
    }

    /**
     * 判断Token是否在30分钟内刚刷新过
     * <p>如果created时间距当前时间小于30分钟，认为是刚刷新的，不再重复刷新
     *
     * @param token 原Token
     * @return true=刚刷新过
     */
    private boolean tokenRefreshJustBefore(String token) {
        Map<String, Object> payload = getPayloadFromToken(token);
        if (payload == null) {
            return false;
        }

        // 1. 获取Token创建时间
        Object created = payload.get(CLAIM_KEY_CREATED);
        Date createdDate = null;
        if (created instanceof Long) {
            createdDate = new Date((Long) created);
        } else if (created instanceof Date) {
            createdDate = (Date) created;
        }
        if (createdDate == null) {
            return false;
        }

        // 2. 判断当前时间是否在 [created, created+30分钟] 区间内
        Date refreshDate = new Date();
        return refreshDate.after(createdDate) && refreshDate.before(DateUtil.offsetSecond(createdDate, 1800));
    }
}
