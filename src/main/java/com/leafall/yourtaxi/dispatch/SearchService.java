package com.leafall.yourtaxi.dispatch;

import com.leafall.yourtaxi.dispatch.GeoService;
import com.leafall.yourtaxi.dto.order.OrderRedisWaitingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.leafall.yourtaxi.dispatch.GeoService.GEO_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    public static final String QUEUE_KEY = "taxi:queue:available";
    public static final String DRIVER_LOCK_PREFIX = "taxi:driver:lock:";
    public static final String ORDERS_KEY = "orders:employees:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final GeoService geoService;

//    public UUID findDriverForOrder(double longitude, double latitude, double radius, UUID orderId) {
//        final var maxRetries = 10;
//        for (int i = 0; i < maxRetries; i++) {
//            var driverIdStr = (String) redisTemplate.opsForList().leftPop(QUEUE_KEY);
//            log.info("Get driver {} id", driverIdStr);
//            if (driverIdStr == null) {
//                log.debug("Queue is empty");
//                return null;
//            }
//            var driverId = UUID.fromString(driverIdStr);
//            var order = getOrderFromRedis(orderId);
//            if (order != null && order.getIds().contains(driverIdStr)) {
//                log.info("Driver {} has already been in this order, we'll skip it.", driverIdStr);
//                continue;
//            }
//            if (Boolean.TRUE.equals(redisTemplate.hasKey(DRIVER_LOCK_PREFIX + driverIdStr))) {
//                log.info("Driver {} is locked, skip", driverIdStr);
//                continue;
//            }
//            var location = geoService.getDriverLocation(driverId);
//            if (location.isEmpty()) {
//                redisTemplate.opsForList().rightPush(QUEUE_KEY, driverIdStr);
//                log.info("No geolocation data for the driver {}", driverIdStr);
//                continue;
//            }
//            var locationEntity = location.get();
//            if (!"FREE".equals(locationEntity.getStatus())) {
//                log.info("Driver has is {} status", locationEntity.getStatus());
//                continue;
//            }
//            log.info("Location get {}", locationEntity);
//            double distance = calculateHaversine(latitude, longitude, locationEntity.getLatitude(), locationEntity.getLongitude());
//            log.info("Distance get {}", distance);
//            if (distance <= radius) {
//                Boolean isLocked = redisTemplate.opsForValue()
//                        .setIfAbsent(DRIVER_LOCK_PREFIX + driverIdStr, "LOCKED", 10, TimeUnit.SECONDS);
//
//                if (Boolean.TRUE.equals(isLocked)) {
//                    log.info("Driver {} found! Distance: {} km", driverIdStr, String.format("%.2f", distance));
//                    return driverId;
//                }
//            } else {
//                log.info("The driver is {} far away ({} km). We're returning to the queue.", driverIdStr, String.format("%.2f", distance));
//                redisTemplate.opsForList().rightPush(QUEUE_KEY, driverIdStr);
//            }
//        }
//        log.warn("Не удалось подобрать водителя за {} итераций", maxRetries);
//        return null;
//    }

    public UUID findDriverForOrder(double longitude, double latitude, double radius, UUID orderId) {
        final var maxRetries = 10;
        int attempts = 0;
        Circle circle = new Circle(
                new Point(longitude, latitude),
                new Distance(radius, Metrics.KILOMETERS)
        );
        var results = redisTemplate.opsForGeo()
                .radius(GEO_KEY, circle, RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()
                        .includeCoordinates()
                        .sortAscending()
                        .limit(20));
        if (results == null || results.getContent().isEmpty()) {
            log.warn("Нет водителей в радиусе {} км", radius);
            return null;
        }
        var order = getOrderFromRedis(orderId);

        for (var result : results.getContent()) {
            if (attempts >= maxRetries) {
                break;
            }
            String driverIdStr = result.getContent().getName().toString();
            var driverId = UUID.fromString(driverIdStr);
            if (order != null && order.getIds().contains(driverIdStr)) {
                log.info("Driver {} has already been in this order, skip", driverIdStr);
                continue;
            }
            if (Boolean.TRUE.equals(redisTemplate.hasKey(DRIVER_LOCK_PREFIX + driverIdStr))) {
                log.info("Driver {} is locked, skip", driverIdStr);
                continue;
            }
            var location = geoService.getDriverLocation(driverId);
            if (location.isEmpty()) {
                log.info("No geolocation data for the driver {}", driverIdStr);
                continue;
            }
            var locationEntity = location.get();
            if (!"FREE".equals(locationEntity.getStatus())) {
                log.info("Driver {} has status {}, skip", driverIdStr, locationEntity.getStatus());
                continue;
            }
            Boolean isLocked = redisTemplate.opsForValue()
                    .setIfAbsent(DRIVER_LOCK_PREFIX + driverIdStr, "LOCKED", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isLocked)) {
                double distance = result.getDistance().getValue();
                log.info("Driver {} found! Distance: {} km",
                        driverIdStr, String.format("%.2f", distance));
                return driverId;
            }
            attempts++;
        }

        log.warn("Не удалось подобрать водителя за {} итераций", maxRetries);
        return null;
    }

    public void addToOrderQueue(OrderRedisWaitingDto order) {
        redisTemplate.opsForValue().set(String.format("%s%s", ORDERS_KEY, order.getId().toString()), order, 30, TimeUnit.MINUTES);
    }

    public OrderRedisWaitingDto getOrderFromRedis(UUID id) {
        return (OrderRedisWaitingDto) redisTemplate.opsForValue().get(String.format("%s%s", ORDERS_KEY, id));
    }
}
