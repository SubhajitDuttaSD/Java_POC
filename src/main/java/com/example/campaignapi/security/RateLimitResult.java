package com.example.campaignapi.security;

public record RateLimitResult(boolean allowed, long remaining, long retryAfterSeconds) {
}
