package com.leafall.yourtaxi.helper.ratelimit;

import com.leafall.yourtaxi.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        var request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        String key = "rate_limit:" + request.getRemoteAddr() + ":" +
                joinPoint.getSignature().getName();

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, rateLimit.timeWindowSeconds(), TimeUnit.SECONDS);
        }
        if (count > rateLimit.maxRequests()) {
            throw new RateLimitException("Превышен лимит: " + rateLimit.maxRequests() +
                    " запросов за " + rateLimit.timeWindowSeconds() + " секунд");
        }

        return joinPoint.proceed();
    }
}
