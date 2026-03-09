package com.sem4.assignments.problem6;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DistributedRateLimiter {
    private static final long ONE_HOUR_MILLIS = 60L * 60L * 1000L;

    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int limitPerHour;

    public DistributedRateLimiter(int limitPerHour) {
        if (limitPerHour <= 0) {
            throw new IllegalArgumentException("limitPerHour must be positive");
        }
        this.limitPerHour = limitPerHour;
    }

    public RateLimitResult checkRateLimit(String clientId) {
        String normalizedClientId = normalize(clientId);
        TokenBucket tokenBucket = buckets.computeIfAbsent(normalizedClientId,
                key -> new TokenBucket(limitPerHour, ONE_HOUR_MILLIS));
        return tokenBucket.consumeOneToken();
    }

    public RateLimitStatus getRateLimitStatus(String clientId) {
        String normalizedClientId = normalize(clientId);
        TokenBucket tokenBucket = buckets.computeIfAbsent(normalizedClientId,
                key -> new TokenBucket(limitPerHour, ONE_HOUR_MILLIS));
        return tokenBucket.snapshot();
    }

    private String normalize(String clientId) {
        return clientId == null ? "" : clientId.trim();
    }

    private static final class TokenBucket {
        private final double maxTokens;
        private final double refillTokensPerMilli;

        private double availableTokens;
        private long lastRefillEpochMillis;

        private TokenBucket(int maxTokens, long refillIntervalMillis) {
            this.maxTokens = maxTokens;
            this.refillTokensPerMilli = maxTokens / (double) refillIntervalMillis;
            this.availableTokens = maxTokens;
            this.lastRefillEpochMillis = System.currentTimeMillis();
        }

        private synchronized RateLimitResult consumeOneToken() {
            long now = System.currentTimeMillis();
            refill(now);

            if (availableTokens >= 1.0) {
                availableTokens -= 1.0;
                return new RateLimitResult(true, (int) Math.floor(availableTokens), 0,
                        "Allowed");
            }

            long retryAfterSeconds = (long) Math.ceil((1.0 - availableTokens) / refillTokensPerMilli / 1000.0);
            return new RateLimitResult(false, 0, Math.max(retryAfterSeconds, 1),
                    "Rate limit exceeded. Try again later.");
        }

        private synchronized RateLimitStatus snapshot() {
            long now = System.currentTimeMillis();
            refill(now);
            int used = (int) (maxTokens - Math.floor(availableTokens));
            long estimatedResetEpoch = now + (long) ((maxTokens - availableTokens) / refillTokensPerMilli);
            return new RateLimitStatus(used, (int) maxTokens, estimatedResetEpoch / 1000L);
        }

        private void refill(long nowEpochMillis) {
            if (nowEpochMillis <= lastRefillEpochMillis) {
                return;
            }

            long elapsedMillis = nowEpochMillis - lastRefillEpochMillis;
            double refillAmount = elapsedMillis * refillTokensPerMilli;
            availableTokens = Math.min(maxTokens, availableTokens + refillAmount);
            lastRefillEpochMillis = nowEpochMillis;
        }
    }

    public record RateLimitResult(
            boolean allowed,
            int remainingRequests,
            long retryAfterSeconds,
            String message
    ) { }

    public record RateLimitStatus(
            int used,
            int limit,
            long resetEpochSeconds
    ) { }
}
