package com.leafall.yourtaxi.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.leafall.yourtaxi.dispatch.GeoService.DRIVER_COORDS_PREFIX;
import static com.leafall.yourtaxi.dispatch.SearchService.DRIVER_LOCK_PREFIX;
import static com.leafall.yourtaxi.dispatch.SearchService.QUEUE_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class DriverDispatchService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void addToQueue(UUID driverId) {
        String locationKey = DRIVER_COORDS_PREFIX + driverId;
        removeFromQueue(driverId);
        redisTemplate.opsForHash().put(locationKey, "status", "FREE");
        redisTemplate.delete(DRIVER_LOCK_PREFIX + driverId);
        log.info("Driver {} add to queue waiting", driverId);
    }

    public void removeFromQueue(UUID driverId) {
        String locationKey = DRIVER_COORDS_PREFIX + driverId;
        redisTemplate.opsForHash().put(locationKey, "status", "BUSY");
    }
}
