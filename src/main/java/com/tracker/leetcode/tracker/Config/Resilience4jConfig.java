package com.tracker.leetcode.tracker.Config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j Configuration for LeetCode API Protection
 *
 * This configuration enables:
 * 1. Circuit Breaker: Prevents cascading failures when LeetCode API is down
 * 2. Retry: Automatically retries failed calls with exponential backoff
 * 3. Rate Limiter: Protects against IP bans by limiting concurrent requests
 *
 * All settings are loaded from application.properties
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

    /**
     * Register event listeners for Circuit Breaker to log state changes
     * This helps monitor API health and resilience triggers
     */
    public Resilience4jConfig(CircuitBreakerRegistry circuitBreakerRegistry,
                             RetryRegistry retryRegistry,
                             RateLimiterRegistry rateLimiterRegistry) {

        // Log Circuit Breaker state transitions
        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(event -> {
                    CircuitBreaker cb = event.getAddedEntry();
                    cb.getEventPublisher()
                            .onStateTransition(e -> log.warn("Circuit Breaker [{}] transitioned from {} to {}",
                                    cb.getName(), e.getStateTransition().getFromState(),
                                    e.getStateTransition().getToState()))
                            .onError(e -> log.error("Circuit Breaker [{}] recorded error: {}",
                                    cb.getName(), e.getThrowable().getMessage()))
                            .onSuccess(e -> log.debug("Circuit Breaker [{}] recorded success", cb.getName()));
                });

        // Log Retry attempts
        retryRegistry.getEventPublisher()
                .onEntryAdded(event -> {
                    Retry retry = event.getAddedEntry();
                    retry.getEventPublisher()
                            .onRetry(e -> log.warn("Retry [{}] attempt {} after error: {}",
                                    retry.getName(), e.getNumberOfRetryAttempts(),
                                    e.getLastThrowable().getMessage()))
                            .onSuccess(e -> log.debug("Retry [{}] succeeded after {} attempts",
                                    retry.getName(), e.getNumberOfRetryAttempts()));
                });

        // Log Rate Limiter events
        rateLimiterRegistry.getEventPublisher()
                .onEntryAdded(event -> {
                    RateLimiter rateLimiter = event.getAddedEntry();
                    rateLimiter.getEventPublisher()
                            .onSuccess(e -> log.debug("Rate Limiter [{}] allowed request", rateLimiter.getName()))
                            .onFailure(e -> log.warn("Rate Limiter [{}] rejected request - too many calls",
                                    rateLimiter.getName()));
                });

        log.info("Resilience4j configuration initialized - Circuit Breaker, Retry, and Rate Limiter are active");
    }
}


