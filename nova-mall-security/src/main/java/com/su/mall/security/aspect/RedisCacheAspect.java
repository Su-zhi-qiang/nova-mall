package com.su.mall.security.aspect;

import com.su.mall.security.annotation.CacheException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Redis缓存AOP异常隔离切面
 * <p>拦截所有 *CacheService 类的方法调用，防止Redis宕机影响正常业务
 * <p>异常处理策略：
 * <ul>
 *   <li>方法标注了 @CacheException → 抛出异常（验证码等关键业务必须感知Redis故障）</li>
 *   <li>方法未标注 @CacheException → 吞掉异常返回null（业务层走降级逻辑直查DB）</li>
 * </ul>
 * <p>@Order(2) 确保在事务切面(@Order(1))之后执行
 *
 * @see CacheException 异常隔离标记注解
 */
@Aspect
@Component
@Order(2)
public class RedisCacheAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    /**
     * 切点：匹配所有 *CacheService 类的public方法
     */
    @Pointcut("execution(public * com.su.mall..service.*CacheService.*(..))")
    public void cacheAspect() {
    }

    /**
     * 环绕通知：拦截缓存方法调用，异常时根据注解决定是否抛出
     */
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取被拦截的方法对象
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        try {
            // 2. 正常执行缓存方法
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            // 3. 异常时检查方法是否标注了 @CacheException
            if (method.isAnnotationPresent(CacheException.class)) {
                // 4. 标注了 @CacheException → 抛出异常（验证码等关键业务必须感知故障）
                throw throwable;
            } else {
                // 5. 未标注 → 吞掉异常，记录日志，返回null（业务层降级处理）
                LOGGER.error("Redis cache operation failed: {}", throwable.getMessage(), throwable);
            }
        }

        // 6. 异常被吞掉时返回null
        return null;
    }
}
