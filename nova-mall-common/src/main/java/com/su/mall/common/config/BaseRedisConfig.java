package com.su.mall.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.su.mall.common.service.RedisService;
import com.su.mall.common.service.impl.RedisServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis基础配置（供各模块继承）
 * <p>定义RedisTemplate、序列化器、缓存管理器和RedisService四个核心Bean
 * <p>序列化策略：Key使用String序列化，Value使用Jackson JSON序列化（支持多态反序列化）
 * <p>缓存管理器默认过期时间：1天
 *
 * @see RedisServiceImpl RedisService实现类
 */
public class BaseRedisConfig {

    /**
     * 配置RedisTemplate
     * <p>统一Key序列化为String，Value序列化为Jackson JSON
     * <p>Hash结构同样采用String Key + JSON Value
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(redisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 创建Jackson JSON序列化器
     * <p>启用DefaultTyping以支持多态反序列化（如List中存储不同子类型）
     * <p>注意：启用DefaultTyping存在安全风险，仅适用于可信环境
     */
    @Bean
    public RedisSerializer<Object> redisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //必须设置，否则无法将JSON转化为对象，会转化成Map类型
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL);
        //创建JSON序列化器
        return new Jackson2JsonRedisSerializer<>(objectMapper,Object.class);
    }

    /**
     * 创建RedisCacheManager
     * <p>使用非锁定写入器（适用于单实例场景）
     * <p>所有缓存条目默认TTL为1天
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        //设置Redis缓存有效期为1天
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer())).entryTtl(Duration.ofDays(1));
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }


    /**
     * 创建RedisService实例
     * <p>封装了String/Hash/Set/List/分布式锁/秒杀库存等常用Redis操作
     */
    @Bean
    public RedisService redisService(RedisTemplate<String, Object> redisTemplate){
        return new RedisServiceImpl(redisTemplate);
    }

}
