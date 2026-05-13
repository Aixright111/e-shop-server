package com.example.e_shop.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Component
public class RedisDistributedLock {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 尝试获取锁
     * @param lockKey 锁的key
     * @param requestId 唯一标识（如UUID，用于区分客户端）
     * @param expireTime 过期时间（毫秒）
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        // SET key value NX PX milliseconds
        // NX: 不存在时才设置（保证互斥）
        // PX: 设置过期时间（防止死锁）
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, requestId, Duration.ofMillis(expireTime));
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放锁（需要验证是否是自己的锁）
     */
    public boolean releaseLock(String lockKey, String requestId) {
        // 使用Lua脚本保证原子性
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('del', KEYS[1]) " +
                        "else " +
                        "    return 0 " +
                        "end";

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(lockKey),
                requestId
        );
        return result != null && result == 1;
    }
}