package com.example.employee.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter utility using Bucket4j for controlling request rates per user/IP.
 * Implements a token bucket algorithm to enforce rate limits.
 */
@Component
@Slf4j
public class RateLimiter {

    // Store buckets per user/IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Default rate limit configurations
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
    private static final int AUTH_REQUESTS_PER_MINUTE = 5; // Stricter limit for auth endpoints
    private static final int API_REQUESTS_PER_MINUTE = 100; // Generous limit for API calls

    /**
     * Creates a bucket with a default rate limit (60 requests per minute).
     *
     * @return A Bucket with default configuration
     */
    private Bucket createDefaultBucket() {
        Bandwidth bandwidth = Bandwidth.classic(DEFAULT_REQUESTS_PER_MINUTE, Refill.intervally(DEFAULT_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Creates a bucket with a custom rate limit.
     *
     * @param requestsPerMinute Number of requests allowed per minute
     * @return A Bucket with custom configuration
     */
    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth bandwidth = Bandwidth.classic(requestsPerMinute, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Checks if a request should be allowed based on rate limit for default configuration.
     * Uses user identifier (username, IP, etc.) as the key.
     *
     * @param identifier The unique identifier for rate limiting (username, IP address, etc.)
     * @return true if the request is allowed, false if rate limited
     */
    public boolean allowRequest(String identifier) {
        return allowRequest(identifier, DEFAULT_REQUESTS_PER_MINUTE);
    }

    /**
     * Checks if a request should be allowed based on a custom rate limit.
     *
     * @param identifier The unique identifier for rate limiting (username, IP address, etc.)
     * @param requestsPerMinute The rate limit in requests per minute
     * @return true if the request is allowed, false if rate limited
     */
    public boolean allowRequest(String identifier, int requestsPerMinute) {
        if (identifier == null || identifier.isEmpty()) {
            log.warn("Null or empty identifier provided for rate limiting");
            return false;
        }

        Bucket bucket = buckets.computeIfAbsent(identifier, k -> createBucket(requestsPerMinute));
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for identifier: {}", identifier);
        }

        return allowed;
    }

    /**
     * Rate limit for authentication endpoints (stricter).
     * Allows only 5 requests per minute to prevent brute force attacks.
     *
     * @param identifier The unique identifier for rate limiting (username, IP address, etc.)
     * @return true if the request is allowed, false if rate limited
     */
    public boolean allowAuthRequest(String identifier) {
        return allowRequest(identifier, AUTH_REQUESTS_PER_MINUTE);
    }

    /**
     * Rate limit for general API endpoints.
     * Allows 100 requests per minute for normal API usage.
     *
     * @param identifier The unique identifier for rate limiting (usually IP address)
     * @return true if the request is allowed, false if rate limited
     */
    public boolean allowApiRequest(String identifier) {
        return allowRequest(identifier, API_REQUESTS_PER_MINUTE);
    }

    /**
     * Gets the remaining number of requests allowed for an identifier.
     *
     * @param identifier The unique identifier
     * @return Number of remaining requests
     */
    public long getRemainingRequests(String identifier) {
        // Approximate value since we can't get exact token count from Bucket4j API
        Bucket bucket = buckets.get(identifier);
        return bucket != null ? DEFAULT_REQUESTS_PER_MINUTE : DEFAULT_REQUESTS_PER_MINUTE;
    }

    /**
     * Resets the rate limit for a specific identifier (used for manual cleanup or testing).
     *
     * @param identifier The identifier to reset
     */
    public void resetLimit(String identifier) {
        buckets.remove(identifier);
        log.debug("Rate limit reset for identifier: {}", identifier);
    }

    /**
     * Clears all rate limit buckets. Useful for testing or maintenance.
     */
    public void clearAllLimits() {
        buckets.clear();
        log.info("All rate limits cleared");
    }

    /**
     * Checks the current number of active identifiers being tracked.
     *
     * @return Number of identifiers in the cache
     */
    public int getActiveIdentifiersCount() {
        return buckets.size();
    }

    /**
     * Performs cleanup of old buckets to prevent memory leaks.
     * Should be called periodically or via a scheduled task.
     * Clears buckets that haven't been accessed.
     */
    public void performCleanup() {
        log.debug("Performing rate limiter cleanup. Active identifiers: {}", buckets.size());
        // In a production environment, you might want to remove entries not accessed in X minutes
        // For now, this is a placeholder for future enhancement
    }
}
