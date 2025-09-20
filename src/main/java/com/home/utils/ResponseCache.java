package com.home.utils;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 响应缓存工具类
 * 缓存处理结果，避免重复计算
 */
@Component
public class ResponseCache {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    /**
     * 获取缓存结果
     */
    public Integer get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            hitCount.incrementAndGet();
            LogUtils.debug("缓存命中: {}", key);
            return entry.getValue();
        }
        missCount.incrementAndGet();
        return null;
    }

    /**
     * 设置缓存结果
     */
    public void put(String key, Integer value, long ttlMs) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMs));
        LogUtils.debug("缓存设置: {} = {}, TTL: {}ms", key, value, ttlMs);
    }

    /**
     * 清理过期缓存
     */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    /**
     * 每5分钟清理一次过期缓存
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void scheduledCleanExpired() {
        try {
            cleanExpired();
            LogUtils.debug("定时清理过期缓存完成");
        } catch (Exception ex) {
            LogUtils.error("清理过期缓存失败: {}", ex.getMessage());
        }
    }

    /**
     * 每小时输出缓存统计信息
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void logCacheStats() {
        try {
            LogUtils.info("缓存统计: {}", getStats());
        } catch (Exception ex) {
            LogUtils.error("获取缓存统计失败: {}", ex.getMessage());
        }
    }

    /**
     * 获取缓存统计
     */
    public CacheStats getStats() {
        return new CacheStats(
            cache.size(),
            hitCount.get(),
            missCount.get()
        );
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final Integer value;
        private final long expireTime;

        public CacheEntry(Integer value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public Integer getValue() { return value; }
        public boolean isExpired() { return isExpired(System.currentTimeMillis()); }
        public boolean isExpired(long now) { return now > expireTime; }
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int size;
        private final long hits;
        private final long misses;

        public CacheStats(int size, long hits, long misses) {
            this.size = size;
            this.hits = hits;
            this.misses = misses;
        }

        public int getSize() { return size; }
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public double getHitRate() { 
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0; 
        }

        @Override
        public String toString() {
            return String.format("CacheStats{size=%d, hits=%d, misses=%d, hitRate=%.2f%%}", 
                size, hits, misses, getHitRate() * 100);
        }
    }
}
