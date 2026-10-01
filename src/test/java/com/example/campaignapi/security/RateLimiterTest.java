package com.example.campaignapi.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    void allowsBurstUpToCapacity_thenDeniesTheNext() {
        RateLimiter rateLimiter = new RateLimiter();
        Long keyId = 1L;

        for (int i = 0; i < RateLimiter.CAPACITY; i++) {
            assertThat(rateLimiter.tryConsume(keyId).allowed()).isTrue();
        }

        RateLimitResult denied = rateLimiter.tryConsume(keyId);
        assertThat(denied.allowed()).isFalse();
        assertThat(denied.retryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    void remainingCountDecreasesWithEachRequest() {
        RateLimiter rateLimiter = new RateLimiter();
        Long keyId = 1L;

        RateLimitResult first = rateLimiter.tryConsume(keyId);
        RateLimitResult second = rateLimiter.tryConsume(keyId);

        assertThat(first.remaining()).isEqualTo(RateLimiter.CAPACITY - 1);
        assertThat(second.remaining()).isEqualTo(RateLimiter.CAPACITY - 2);
    }

    @Test
    void differentKeysHaveIndependentBuckets() {
        RateLimiter rateLimiter = new RateLimiter();
        Long exhaustedKey = 1L;
        Long freshKey = 2L;

        for (int i = 0; i < RateLimiter.CAPACITY; i++) {
            rateLimiter.tryConsume(exhaustedKey);
        }
        assertThat(rateLimiter.tryConsume(exhaustedKey).allowed()).isFalse();

        assertThat(rateLimiter.tryConsume(freshKey).allowed()).isTrue();
    }

    @Test
    void refillsOverTime() throws InterruptedException {
        RateLimiter rateLimiter = new RateLimiter();
        Long keyId = 1L;

        for (int i = 0; i < RateLimiter.CAPACITY; i++) {
            rateLimiter.tryConsume(keyId);
        }
        assertThat(rateLimiter.tryConsume(keyId).allowed()).isFalse();

        Thread.sleep(RateLimiter.REFILL_INTERVAL_MILLIS + 200);

        assertThat(rateLimiter.tryConsume(keyId).allowed()).isTrue();
    }
}
