package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.props.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean getEnabled() {
        return properties.getEnabled();
    }

    public boolean tryConsumeLogin(String key) {
        RateLimitProperties.EndpointConfig config = properties.getLogin();
        return tryConsume(key, config.getMaxAttempts(), config.getWindowMinutes());
    }

    public boolean tryConsumeRegistration(String key) {
        RateLimitProperties.EndpointConfig config = properties.getRegistration();
        return tryConsume(key, config.getMaxAttempts(), config.getWindowMinutes());
    }

    public boolean tryConsumeApi(String key) {
        RateLimitProperties.EndpointConfig config = properties.getApi();
        return tryConsume(key, config.getMaxAttempts(), config.getWindowMinutes());
    }

    private boolean tryConsume(String key, int maxRequests, int windowMinutes) {
        var bucket = buckets.computeIfAbsent(key, k -> createBucket(maxRequests, windowMinutes));

        var probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            log.debug("Request consumed. Remaining: {}", probe.getRemainingTokens());
            return true;
        } else {
            long waitTime = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded for key: {}. Wait time: {} seconds", key, waitTime);
            return false;
        }
    }

    private Bucket createBucket(int maxRequests, int windowMinutes) {
        var refillPeriod = Duration.ofMinutes(windowMinutes);
        var refill = Refill.greedy(maxRequests, refillPeriod);
        var limit = Bandwidth.classic(maxRequests, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
