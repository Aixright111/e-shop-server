package com.example.e_shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存命中/未命中计数器。
 *
 * 由 MonitoredCache 在每次执行 Cache.get() 时调用 recordHit() / recordMiss()，
 * 并定时通过 @Scheduled 打印所有缓存空间的命中率报告。
 *
 * 输出示例：
 *   Cache Monitor Report:
 *     songCache      | hits=42  | misses=8   | total=50  | hitRate=84.0%
 *     bannerCache    | hits=200 | misses=0   | total=200 | hitRate=100.0%
 *
 * 需要主启动类添加 @EnableScheduling 使定时任务生效。
 */
@Slf4j
@Component
public class CacheMonitor {

    /** 每个缓存空间的命中次数 */
    private final Map<String, AtomicLong> hitMap = new ConcurrentHashMap<>();

    /** 每个缓存空间的未命中次数 */
    private final Map<String, AtomicLong> missMap = new ConcurrentHashMap<>();

    /**
     * 记录一次命中。
     * @param cacheName 缓存空间名称（如 songCache、artistCache）
     */
    public void recordHit(String cacheName) {
        hitMap.computeIfAbsent(cacheName, k -> new AtomicLong()).incrementAndGet();
    }

    /**
     * 记录一次未命中。
     * @param cacheName 缓存空间名称
     */
    public void recordMiss(String cacheName) {
        missMap.computeIfAbsent(cacheName, k -> new AtomicLong()).incrementAndGet();
    }

    /**
     * 定时打印所有缓存空间的命中率报告，每 60 秒执行一次。
     */
    @Scheduled(fixedRate = 60000)
    public void report() {
        // 合并所有出现过的缓存空间名称
        hitMap.keySet().stream()
                .sorted()
                .forEach(name -> {
                    long hits = hitMap.getOrDefault(name, new AtomicLong()).get();
                    long misses = missMap.getOrDefault(name, new AtomicLong()).get();
                    long total = hits + misses;
                    double rate = total > 0 ? (double) hits / total * 100 : 0;
                    log.info("Cache[{}] | hits={} | misses={} | total={} | hitRate={}%",
                            name, hits, misses, total, String.format("%.1f", rate));
                });
    }
}
