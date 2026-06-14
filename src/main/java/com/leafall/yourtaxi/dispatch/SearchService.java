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
    private final OrderAssignmentService orderAssignmentService;

    public UUID findDriverForOrder(double longitude, double latitude, double radius, UUID orderId) {
        return orderAssignmentService.findDriverForOrder(longitude, latitude, radius, orderId);
    }

    public void addToOrderQueue(OrderRedisWaitingDto order) {
        redisTemplate.opsForValue().set(String.format("%s%s", ORDERS_KEY, order.getId().toString()), order, 30, TimeUnit.MINUTES);
    }

    public OrderRedisWaitingDto getOrderFromRedis(UUID id) {
        var order = redisTemplate.opsForValue().get(String.format("%s%s", ORDERS_KEY, id));
        if (order == null) {
            return null;
        }
        return (OrderRedisWaitingDto) order;
    }
}
