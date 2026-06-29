package com.su.mall.common.service.impl;

import com.su.mall.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作Service实现类
 * <p>封装RedisTemplate，提供String/Hash/Set/List等数据结构操作
 * <p>核心功能：秒杀库存Lua原子预减、优惠券库存预减、分布式锁释放
 *
 * @see RedisService 接口定义
 */
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void del(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void del(List<String> keys) {
        redisTemplate.delete(keys);
    }

    @Override
    public Boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    @Override
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    @Override
    public Boolean hSet(String key, String hashKey, Object value, long time) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        return expire(key, time);
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public Boolean hSetAll(String key, Map<String, Object> map, long time) {
        redisTemplate.opsForHash().putAll(key, map);
        return expire(key, time);
    }

    @Override
    public void hSetAll(String key, Map<String, ?> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public void hDel(String key, Object... hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Long hIncr(String key, String hashKey, Long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    @Override
    public Long hDecr(String key, String hashKey, Long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, -delta);
    }

    @Override
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Long sAdd(String key, long time, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        expire(key, time);
        return count;
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    @Override
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    @Override
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Long lPush(String key, Object value, long time) {
        Long index = redisTemplate.opsForList().rightPush(key, value);
        expire(key, time);
        return index;
    }

    @Override
    public Long lPushAll(String key, Object... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public Long lPushAll(String key, Long time, Object... values) {
        Long count = redisTemplate.opsForList().rightPushAll(key, values);
        expire(key, time);
        return count;
    }

    @Override
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    /**
     * 尝试获取分布式锁（SET NX + 过期时间，原子操作）
     *
     * @param key   锁的key
     * @param value 锁的value（通常是UUID，用于安全释放）
     * @param time  锁的过期时间（秒）
     * @return true=获取成功, false=已被其他线程持有
     */
    @Override
    public Boolean tryLock(String key, Object value, long time) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 安全释放分布式锁（Lua脚本保证原子性）
     * <p>只有持有锁的线程才能释放，防止误删其他线程的锁
     * <p>Lua逻辑：GET锁的value → 比较是否与传入value一致 → 一致则DEL
     *
     * @param key   锁的key
     * @param value 锁的value（必须与tryLock时传入的一致）
     * @return true=释放成功, false=锁已过期或不属于当前线程
     */
    @Override
    public Boolean releaseLock(String key, Object value) {
        // 1. 定义Lua脚本：先比较value再删除，保证原子性
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        // 2. 执行Lua脚本
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return result != null && result > 0;
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public void del(Set<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // ==================== 秒杀库存 Lua 脚本 ====================

    /**
     * 秒杀库存预减Lua脚本
     * <p>逻辑：读取库存 → 判断nil/≤0 → 原子扣减
     * <p>返回值：-1=库存key不存在, 0=库存不足, 1=扣减成功
     * <p>Redis单线程执行Lua脚本，天然保证原子性，防止并发超卖
     */
    private static final String SECKILL_STOCK_DEDUCT_SCRIPT =
            "local stock = tonumber(redis.call('get', KEYS[1])) " +  // 1. 读取当前库存
            "if stock == nil then return -1 end " +                    // 2. key不存在返回-1
            "if stock <= 0 then return 0 end " +                      // 3. 库存不足返回0
            "redis.call('decrby', KEYS[1], ARGV[1]) " +               // 4. 原子扣减库存
            "return 1";                                                // 5. 扣减成功返回1

    /**
     * 优惠券库存预减Lua脚本
     * <p>与秒杀类似，每次扣减1张（decr而非decrby）
     */
    private static final String COUPON_STOCK_DEDUCT_SCRIPT =
            "local stock = tonumber(redis.call('get', KEYS[1])) " +
            "if stock == nil then return -1 end " +
            "if stock <= 0 then return 0 end " +
            "redis.call('decr', KEYS[1]) " +
            "return 1";

    private static final StringRedisSerializer stringSerializer = new StringRedisSerializer();

    /**
     * 秒杀库存预减（Lua原子操作）
     * <p>通过RedisCallback直接操作connection执行Lua脚本，避免Jackson序列化干扰
     *
     * @param relationId 秒杀关联ID
     * @return true=预减成功, false=库存不足, null=库存key不存在（活动未开始）
     */
    @Override
    public Boolean deductSeckillStock(Long relationId) {
        // 1. 构建Redis key
        String key = "seckill:stock:" + relationId;

        // 2. 通过RedisCallback执行Lua脚本（保证序列化一致性）
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            byte[] scriptBytes = SECKILL_STOCK_DEDUCT_SCRIPT.getBytes();
            byte[] keyBytes = stringSerializer.serialize(key);
            byte[] argBytes = stringSerializer.serialize("1"); // 扣减数量=1
            Object raw = connection.eval(scriptBytes,
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1, keyBytes, argBytes);                    // 1个key
            return raw instanceof Long ? (Long) raw : null;
        });

        // 3. 解析返回值
        if (result == null || result == -1) {
            return null; // 库存key不存在（活动未开始或已过期）
        }
        return result == 1; // true=成功, false=库存不足
    }

    /**
     * 秒杀库存恢复（乐观补偿）
     * <p>在下单失败或订单取消时，将已预扣的库存加回去
     *
     * @param relationId 秒杀关联ID
     * @param quantity   恢复数量
     */
    @Override
    public void restoreSeckillStock(Long relationId, int quantity) {
        String key = "seckill:stock:" + relationId;
        redisTemplate.opsForValue().increment(key, quantity);
    }

    /**
     * 优惠券库存预减（Lua原子操作）
     * <p>与秒杀库存预减逻辑一致，key前缀为 coupon:stock:
     *
     * @param couponId 优惠券ID
     * @return true=预减成功, false=库存不足, null=库存key不存在
     */
    @Override
    public Boolean deductCouponStock(Long couponId) {
        String key = "coupon:stock:" + couponId;
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            byte[] scriptBytes = COUPON_STOCK_DEDUCT_SCRIPT.getBytes();
            byte[] keyBytes = stringSerializer.serialize(key);
            Object raw = connection.eval(scriptBytes,
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1, keyBytes);
            return raw instanceof Long ? (Long) raw : null;
        });
        if (result == null || result == -1) {
            return null;
        }
        return result == 1;
    }

    /**
     * 优惠券库存恢复
     *
     * @param couponId 优惠券ID
     */
    @Override
    public void restoreCouponStock(Long couponId) {
        String key = "coupon:stock:" + couponId;
        redisTemplate.opsForValue().increment(key);
    }
}
