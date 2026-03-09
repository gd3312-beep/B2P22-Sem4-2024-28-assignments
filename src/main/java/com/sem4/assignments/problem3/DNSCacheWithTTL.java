package com.sem4.assignments.problem3;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class DNSCacheWithTTL implements AutoCloseable {
    private final Map<String, DNSEntry> cache;
    private final UpstreamDnsResolver upstreamDnsResolver;
    private final ScheduledExecutorService cleanupExecutor;

    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder lookups = new LongAdder();
    private final LongAdder totalLookupNanos = new LongAdder();

    public DNSCacheWithTTL(int maxSize, UpstreamDnsResolver upstreamDnsResolver) {
        int capacity = Math.max(1, maxSize);
        this.upstreamDnsResolver = upstreamDnsResolver;
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > capacity;
            }
        };

        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.cleanupExecutor.scheduleAtFixedRate(this::evictExpiredEntries, 1, 1, TimeUnit.SECONDS);
    }

    public ResolveResult resolve(String domain) {
        String normalizedDomain = domain == null ? "" : domain.trim().toLowerCase();
        long now = System.currentTimeMillis();
        long startNanos = System.nanoTime();
        boolean expired = false;

        lookups.increment();

        synchronized (cache) {
            DNSEntry cachedEntry = cache.get(normalizedDomain);
            if (cachedEntry != null) {
                if (cachedEntry.expiryEpochMillis > now) {
                    cachedEntry.lastAccessEpochMillis = now;
                    hits.increment();
                    long lookupNanos = System.nanoTime() - startNanos;
                    totalLookupNanos.add(lookupNanos);
                    long ttlRemainingSeconds = Math.max(0, (cachedEntry.expiryEpochMillis - now) / 1000);
                    return new ResolveResult(normalizedDomain, cachedEntry.ipAddress, CacheSource.HIT,
                            ttlRemainingSeconds, nanosToMillis(lookupNanos));
                }

                expired = true;
                cache.remove(normalizedDomain);
            }
        }

        misses.increment();
        ResolvedAddress upstreamResolved = upstreamDnsResolver.resolve(normalizedDomain);
        long expiryEpochMillis = now + (upstreamResolved.ttlSeconds() * 1000L);

        synchronized (cache) {
            cache.put(normalizedDomain, new DNSEntry(upstreamResolved.ipAddress(), expiryEpochMillis, now));
        }

        long lookupNanos = System.nanoTime() - startNanos;
        totalLookupNanos.add(lookupNanos);
        CacheSource source = expired ? CacheSource.EXPIRED : CacheSource.MISS;
        return new ResolveResult(normalizedDomain, upstreamResolved.ipAddress(), source,
                upstreamResolved.ttlSeconds(), nanosToMillis(lookupNanos));
    }

    public CacheStats getCacheStats() {
        long hitCount = hits.sum();
        long missCount = misses.sum();
        long total = hitCount + missCount;
        double hitRate = total == 0 ? 0.0 : (hitCount * 100.0) / total;
        double averageLookupMillis = lookups.sum() == 0 ? 0.0 : nanosToMillis(totalLookupNanos.sum() / (double) lookups.sum());

        synchronized (cache) {
            return new CacheStats(hitCount, missCount, hitRate, averageLookupMillis, cache.size());
        }
    }

    private void evictExpiredEntries() {
        long now = System.currentTimeMillis();
        synchronized (cache) {
            Iterator<Map.Entry<String, DNSEntry>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, DNSEntry> entry = iterator.next();
                if (entry.getValue().expiryEpochMillis <= now) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void close() {
        cleanupExecutor.shutdownNow();
    }

    private double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }

    private static final class DNSEntry {
        private final String ipAddress;
        private final long expiryEpochMillis;
        private volatile long lastAccessEpochMillis;

        private DNSEntry(String ipAddress, long expiryEpochMillis, long lastAccessEpochMillis) {
            this.ipAddress = ipAddress;
            this.expiryEpochMillis = expiryEpochMillis;
            this.lastAccessEpochMillis = lastAccessEpochMillis;
        }
    }

    @FunctionalInterface
    public interface UpstreamDnsResolver {
        ResolvedAddress resolve(String domain);
    }

    public record ResolvedAddress(String ipAddress, long ttlSeconds) { }

    public record ResolveResult(
            String domain,
            String ipAddress,
            CacheSource source,
            long ttlSeconds,
            double lookupTimeMillis
    ) { }

    public record CacheStats(
            long hits,
            long misses,
            double hitRatePercent,
            double averageLookupTimeMillis,
            int cacheSize
    ) { }

    public enum CacheSource {
        HIT,
        MISS,
        EXPIRED
    }
}
