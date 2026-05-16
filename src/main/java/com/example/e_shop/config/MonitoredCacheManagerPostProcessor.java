package com.example.e_shop.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * BeanPostProcessor：在 RedisCacheManager 初始化完成后，将其替换为装饰器版本。
 *
 * 装饰器覆盖 getCache() 方法，使每次返回的 Cache 对象都被 MonitoredCache 包裹，
 * 从而在 Cache.get() 调用时自动统计命中/未命中次数。
 *
 * 优先级说明：
 *   BeanPostProcessor 在 Spring Bean 初始化后、其他 Bean 使用前执行，
 *   对 CacheManager 的替换对业务代码完全透明。
 *
 * 不使用 @Primary 或修改 RedisConfig，
 * 避免与原有 RedisCacheManager 的 @Bean 声明冲突。
 */
@Component
public class MonitoredCacheManagerPostProcessor implements BeanPostProcessor {

    @Autowired
    private CacheMonitor cacheMonitor;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 识别 RedisConfig 中定义的 RedisCacheManager
        if (bean instanceof CacheManager original) {
            // 用装饰器替换原始 CacheManager
            return new MonitoredCacheManager(original, cacheMonitor);
        }
        return bean;
    }

    /**
     * CacheManager 装饰器：拦截 getCache()，用 MonitoredCache 包裹原始 Cache。
     */
    private static class MonitoredCacheManager implements CacheManager {

        private final CacheManager delegate;
        private final CacheMonitor monitor;

        /** 缓存已包裹的 Cache 实例，避免重复创建 MonitoredCache 对象 */
        private final ConcurrentMap<String, Cache> wrapped = new ConcurrentHashMap<>();

        MonitoredCacheManager(CacheManager delegate, CacheMonitor monitor) {
            this.delegate = delegate;
            this.monitor = monitor;
        }

        @Override
        public Cache getCache(String name) {
            // computeIfAbsent 保证每个缓存空间只被包裹一次
            return wrapped.computeIfAbsent(name, n -> {
                Cache original = delegate.getCache(n);
                if (original == null) {
                    return null;
                }
                return new MonitoredCache(n, original, monitor);
            });
        }

        @Override
        public Collection<String> getCacheNames() {
            return delegate.getCacheNames();
        }
    }
}
