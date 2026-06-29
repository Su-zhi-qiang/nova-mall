package com.su.mall.security.annotation;

import java.lang.annotation.*;

/**
 * 缓存异常隔离标记注解
 * <p>标注在CacheService的方法上，表示该方法在Redis异常时必须抛出异常
 * <p>未标注此注解的方法，Redis异常会被 {@link com.su.mall.security.aspect.RedisCacheAspect} 吞掉并返回null
 * <p>典型场景：验证码的读写必须感知Redis故障，不能静默失败
 *
 * @see com.su.mall.security.aspect.RedisCacheAspect
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheException {
}
