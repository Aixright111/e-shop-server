package com.example.e_shop.config;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Cache 装饰器：在原始 Cache.get() 调用前后插入命中/未命中统计。
 *
 * 只重写 get() 相关方法用于统计，put / evict / clear 直接委托给原始 Cache。
 */
public class MonitoredCache implements Cache {

    /** 缓存空间名称（如 songCache、artistCache） */
    private final String name;

    /** 被装饰的原始 RedisCache */
    private final Cache delegate;

    /** 统计计数器 */
    private final CacheMonitor monitor;

    public MonitoredCache(String name, Cache delegate, CacheMonitor monitor) {
        this.name = name;
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    /**
     * 核心拦截点：每次缓存查询都经过这里统计命中/未命中。
     */
    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper value = delegate.get(key);
        if (value != null) {
            monitor.recordHit(this.name);
        } else {
            monitor.recordMiss(this.name);
        }
        return value;
    }

    /**
     * 默认实现会调用 get(Object key)，命中统计已在 get(key) 中完成。
     */
    @Override
    public <T> T get(Object key, Class<T> type) {
        return delegate.get(key, type);
    }

    /**
     * 默认实现会调用 get(Object key)，命中统计已在 get(key) 中完成。
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        delegate.put(key, value);
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
