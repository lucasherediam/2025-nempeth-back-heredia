package com.nempeth.korven.service;

import com.nempeth.korven.exception.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    @DisplayName("Should allow requests within the limit")
    void shouldAllowRequestsWithinTheLimit() {
        // Given
        String key = "login:user@test.com";
        int maxAttempts = 5;
        Duration window = Duration.ofMinutes(1);

        // When / Then — should not throw
        for (int i = 0; i < maxAttempts; i++) {
            assertThatCode(() -> rateLimiterService.checkRateLimit(key, maxAttempts, window))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("Should reject when limit is exceeded")
    void shouldRejectWhenLimitIsExceeded() {
        // Given
        String key = "login:user@test.com";
        int maxAttempts = 3;
        Duration window = Duration.ofMinutes(1);

        // Fill up the limit
        for (int i = 0; i < maxAttempts; i++) {
            rateLimiterService.checkRateLimit(key, maxAttempts, window);
        }

        // When / Then
        assertThatThrownBy(() -> rateLimiterService.checkRateLimit(key, maxAttempts, window))
                .isInstanceOf(RateLimitException.class)
                .hasMessage("Demasiados intentos. Intentá de nuevo más tarde.");
    }

    @Test
    @DisplayName("Should track keys independently")
    void shouldTrackKeysIndependently() {
        // Given
        int maxAttempts = 2;
        Duration window = Duration.ofMinutes(1);

        // Fill up limit for key1
        rateLimiterService.checkRateLimit("login:user1@test.com", maxAttempts, window);
        rateLimiterService.checkRateLimit("login:user1@test.com", maxAttempts, window);

        // When / Then — key1 should be blocked, key2 still allowed
        assertThatThrownBy(() -> rateLimiterService.checkRateLimit("login:user1@test.com", maxAttempts, window))
                .isInstanceOf(RateLimitException.class);

        assertThatCode(() -> rateLimiterService.checkRateLimit("login:user2@test.com", maxAttempts, window))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow requests after window expires")
    void shouldAllowRequestsAfterWindowExpires() {
        // Given — use a very short window
        String key = "login:user@test.com";
        int maxAttempts = 2;
        Duration window = Duration.ofMillis(50);

        // Fill up the limit
        rateLimiterService.checkRateLimit(key, maxAttempts, window);
        rateLimiterService.checkRateLimit(key, maxAttempts, window);

        // Wait for the window to expire
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When / Then — should be allowed again
        assertThatCode(() -> rateLimiterService.checkRateLimit(key, maxAttempts, window))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reset all state")
    void shouldResetAllState() {
        // Given
        String key = "login:user@test.com";
        int maxAttempts = 1;
        Duration window = Duration.ofMinutes(1);

        rateLimiterService.checkRateLimit(key, maxAttempts, window);

        // Blocked now
        assertThatThrownBy(() -> rateLimiterService.checkRateLimit(key, maxAttempts, window))
                .isInstanceOf(RateLimitException.class);

        // When
        rateLimiterService.reset();

        // Then — should be allowed again
        assertThatCode(() -> rateLimiterService.checkRateLimit(key, maxAttempts, window))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should work with different rate limit configurations")
    void shouldWorkWithDifferentConfigurations() {
        // Given — same key but stricter limit
        String key = "forgot:user@test.com";

        // 3 attempts allowed
        rateLimiterService.checkRateLimit(key, 3, Duration.ofMinutes(1));
        rateLimiterService.checkRateLimit(key, 3, Duration.ofMinutes(1));
        rateLimiterService.checkRateLimit(key, 3, Duration.ofMinutes(1));

        // When / Then — 4th should fail
        assertThatThrownBy(() -> rateLimiterService.checkRateLimit(key, 3, Duration.ofMinutes(1)))
                .isInstanceOf(RateLimitException.class);
    }
}
