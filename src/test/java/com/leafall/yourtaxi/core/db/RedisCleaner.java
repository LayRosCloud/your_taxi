package com.leafall.yourtaxi.core.db;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCleaner {
    private final RedisTemplate<String, Object> redisTemplate;

    public void clear() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
           connection.serverCommands().flushDb();
            return null;
        });
    }
}
