package com.example.campaignapi.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-key token bucket: a burst of {@link #CAPACITY} requests, refilled at
 * one token every {@link #REFILL_INTERVAL_MILLIS}. In-memory and per-instance —
 * a multi-instance deployment would need a shared store (e.g. Redis) instead.
 */
@Component
public class RateLimiter {

    static final long CAPACITY = 5;
    static final long REFILL_INTERVAL_MILLIS = 2000;

    private final Map<Long, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitResult tryConsume(Long apiKeyId) {
        TokenBucket bucket = buckets.computeIfAbsent(apiKeyId, id -> new TokenBucket());
        return bucket.tryConsume();
    }

    private static final class TokenBucket {
        private double tokens = CAPACITY;
        private long lastRefillTimestamp = System.currentTimeMillis();

        synchronized RateLimitResult tryConsume() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return new RateLimitResult(true, (long) Math.floor(tokens), 0);
            }
            long retryAfterSeconds = (long) Math.ceil((1 - tokens) * REFILL_INTERVAL_MILLIS / 1000.0);
            return new RateLimitResult(false, 0, Math.max(retryAfterSeconds, 1));
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed <= 0) {
                return;
            }
            double tokensToAdd = elapsed / (double) REFILL_INTERVAL_MILLIS;
            tokens = Math.min(CAPACITY, tokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}
