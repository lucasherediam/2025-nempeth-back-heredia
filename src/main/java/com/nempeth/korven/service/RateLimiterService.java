package com.nempeth.korven.service;

import com.nempeth.korven.exception.RateLimitException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory sliding-window rate limiter.
 * Tracks timestamps of attempts per key and rejects when the limit is exceeded.
 */
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    /**
     * Checks whether the given key has exceeded the allowed number of attempts
     * within the specified time window. If exceeded, throws {@link RateLimitException}.
     *
     * @param key         identifier (e.g. "login:user@example.com")
     * @param maxAttempts max allowed attempts in the window
     * @param window      sliding time window
     */
    public void checkRateLimit(String key, int maxAttempts, Duration window) {
        Deque<Instant> timestamps = attempts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        Instant cutoff = Instant.now().minus(window);

        // Evict expired entries
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxAttempts) {
            throw new RateLimitException("Demasiados intentos. Intentá de nuevo más tarde.");
        }

        timestamps.addLast(Instant.now());
    }

    /** Visible for testing — clears all tracked state. */
    void reset() {
        attempts.clear();
    }
}
