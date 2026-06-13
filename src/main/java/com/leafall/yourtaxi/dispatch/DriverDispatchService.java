package com.leafall.yourtaxi.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.leafall.yourtaxi.dispatch.SearchService.DRIVER_LOCK_PREFIX;

@Component
@RequiredArgsConstructor
@Slf4j
public class DriverDispatchService {
    private final RedisTemplate<String, Object> redisTemplate;
    public final static String DRIVER_STATUS = "taxi:driver:";

    public void addToQueue(UUID driverId) {
        String locationKey = DRIVER_STATUS + driverId;
        removeFromQueue(driverId);
        redisTemplate.opsForHash().put(locationKey, "status", "FREE");
        redisTemplate.delete(DRIVER_LOCK_PREFIX + driverId);
        log.info("Driver {} add to queue waiting", driverId);
    }

    public void removeFromQueue(UUID driverId) {
        String locationKey = DRIVER_STATUS + driverId;
        redisTemplate.opsForHash().put(locationKey, "status", "BUSY");
    }
}
