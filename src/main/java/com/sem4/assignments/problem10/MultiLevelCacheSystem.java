package com.sem4.assignments.problem10;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class MultiLevelCacheSystem {
    private static final double L1_LATENCY_MS = 0.5;
    private static final double L2_LATENCY_MS = 5.0;
    private static final double L3_LATENCY_MS = 150.0;

    private final Map<String, VideoData> l1Cache;
    private final Map<String, VideoData> l2Cache;
    private final ConcurrentMap<String, VideoData> l3Database = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> accessCount = new ConcurrentHashMap<>();

    private final int promotionThreshold;

    private final LongAdder l1Hits = new LongAdder();
    private final LongAdder l2Hits = new LongAdder();
    private final LongAdder l3Hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalLatencyMicros = new LongAdder();

    public MultiLevelCacheSystem(int l1Capacity, int l2Capacity, int promotionThreshold) {
        this.l1Cache = new LruCache<>(Math.max(1, l1Capacity));
        this.l2Cache = new LruCache<>(Math.max(1, l2Capacity));
        this.promotionThreshold = Math.max(1, promotionThreshold);
    }

    public void putInDatabase(VideoData videoData) {
        l3Database.put(videoData.videoId(), videoData);
    }

    public CacheLookupResult getVideo(String videoId) {
        totalRequests.increment();

        VideoData fromL1;
        synchronized (l1Cache) {
            fromL1 = l1Cache.get(videoId);
        }
        if (fromL1 != null) {
            l1Hits.increment();
            int count = increaseAccess(videoId);
            double latency = L1_LATENCY_MS;
            recordLatency(latency);
            return new CacheLookupResult(fromL1, CacheLevel.L1, latency, false, count);
        }

        VideoData fromL2;
        synchronized (l2Cache) {
            fromL2 = l2Cache.get(videoId);
        }
        if (fromL2 != null) {
            l2Hits.increment();
            int count = increaseAccess(videoId);
            boolean promoted = promoteIfEligible(videoId, fromL2, count);
            double latency = L2_LATENCY_MS;
            recordLatency(latency);
            return new CacheLookupResult(fromL2, CacheLevel.L2, latency, promoted, count);
        }

        VideoData fromL3 = l3Database.get(videoId);
        if (fromL3 != null) {
            l3Hits.increment();
            synchronized (l2Cache) {
                l2Cache.put(videoId, fromL3);
            }
            int count = increaseAccess(videoId);
            boolean promoted = promoteIfEligible(videoId, fromL3, count);
            double latency = L3_LATENCY_MS;
            recordLatency(latency);
            return new CacheLookupResult(fromL3, CacheLevel.L3, latency, promoted, count);
        }

        misses.increment();
        recordLatency(L3_LATENCY_MS);
        return new CacheLookupResult(null, CacheLevel.MISS, L3_LATENCY_MS, false, 0);
    }

    public void invalidateVideo(String videoId) {
        synchronized (l1Cache) {
            l1Cache.remove(videoId);
        }
        synchronized (l2Cache) {
            l2Cache.remove(videoId);
        }
        l3Database.remove(videoId);
        accessCount.remove(videoId);
    }

    public CacheStatistics getStatistics() {
        long l1 = l1Hits.sum();
        long l2 = l2Hits.sum();
        long l3 = l3Hits.sum();
        long miss = misses.sum();
        long total = l1 + l2 + l3 + miss;

        double l1Rate = total == 0 ? 0.0 : (l1 * 100.0) / total;
        double l2Rate = total == 0 ? 0.0 : (l2 * 100.0) / total;
        double l3Rate = total == 0 ? 0.0 : (l3 * 100.0) / total;
        double overallHitRate = total == 0 ? 0.0 : ((l1 + l2 + l3) * 100.0) / total;
        double avgLatency = totalRequests.sum() == 0 ? 0.0 : (totalLatencyMicros.sum() / 1000.0) / totalRequests.sum();

        int l1Size;
        int l2Size;
        synchronized (l1Cache) {
            l1Size = l1Cache.size();
        }
        synchronized (l2Cache) {
            l2Size = l2Cache.size();
        }

        return new CacheStatistics(l1Rate, l2Rate, l3Rate, overallHitRate, avgLatency, l1Size, l2Size, l3Database.size());
    }

    private void recordLatency(double millis) {
        totalLatencyMicros.add((long) (millis * 1000));
    }

    private int increaseAccess(String videoId) {
        return accessCount.computeIfAbsent(videoId, key -> new AtomicInteger()).incrementAndGet();
    }

    private boolean promoteIfEligible(String videoId, VideoData videoData, int count) {
        if (count < promotionThreshold) {
            return false;
        }

        synchronized (l1Cache) {
            l1Cache.put(videoId, videoData);
        }
        return true;
    }

    private static final class LruCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        private LruCache(int capacity) {
            super(16, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    public enum CacheLevel {
        L1,
        L2,
        L3,
        MISS
    }

    public record VideoData(String videoId, String title, String payload, long updatedAtEpochMillis) { }

    public record CacheLookupResult(
            VideoData videoData,
            CacheLevel level,
            double latencyMillis,
            boolean promotedToL1,
            int accessCount
    ) { }

    public record CacheStatistics(
            double l1HitRate,
            double l2HitRate,
            double l3HitRate,
            double overallHitRate,
            double averageLatencyMillis,
            int l1Size,
            int l2Size,
            int l3Size
    ) { }
}
