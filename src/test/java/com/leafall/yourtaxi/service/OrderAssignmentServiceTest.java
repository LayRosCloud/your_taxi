package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.core.db.OrderDbHelper;
import com.leafall.yourtaxi.dispatch.OrderAssignmentService;
import com.leafall.yourtaxi.dto.OfferAssignment;
import com.leafall.yourtaxi.utils.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.leafall.yourtaxi.dispatch.OrderAssignmentService.ACTIVE_OFFERS_KEY;
import static org.junit.jupiter.api.Assertions.*;

public class OrderAssignmentServiceTest extends BaseIntegrationTest {

    @Autowired
    private OrderAssignmentService service;
    @Autowired
    private OrderDbHelper orderDbHelper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisCleaner.clear();
    }

    @Test
    public void createOffer_happyPath() {
        // given
        var driverId = UUID.fromString("a1aaba2a-3f07-4d62-b47b-09d5f0708815");
        var order = orderDbHelper.save();
        // when
        service.createOffer(driverId, order.getId());
        // then
        assertEquals(Boolean.TRUE, redisTemplate.hasKey(ACTIVE_OFFERS_KEY));
        var set = redisTemplate.opsForZSet().range(ACTIVE_OFFERS_KEY, 0, -1);
        assertEquals(1, set.size());
        var offer = objectMapper.readValue(set.toArray()[0].toString(), OfferAssignment.class) ;
        assertEquals(order.getId(), offer.getOrderId());
        assertEquals(order.getUser().getId(), offer.getUserId());
        assertEquals(driverId, offer.getDriverId());
        assertNotNull(offer.getExpiresAt());
        assertNotNull(offer.getCreatedAt());
    }
    @Test
    public void debugLuaArguments() {
        var driverId = UUID.fromString("a1aaba2a-3f07-4d62-b47b-09d5f0708815");
        var orderId = UUID.fromString("a1aaba2a-3f07-4d62-b47b-09d5f0708816");

        // Тестовый скрипт, который возвращает полученные аргументы
        String debugScript =
                "return {ARGV[1], ARGV[2]}";

        DefaultRedisScript<List> script = new DefaultRedisScript<>(debugScript, List.class);
        List result = redisTemplate.execute(script, Collections.singletonList(ACTIVE_OFFERS_KEY),
                orderId.toString(), driverId.toString());

        System.out.println("Received args: " + result);
        System.out.println("Expected orderId: " + orderId);
        System.out.println("Expected driverId: " + driverId);
        assertEquals(orderId.toString(), result.get(0).toString());
        assertEquals(driverId.toString(), result.get(1).toString());
    }

    @Test
    public void createOfferAndRemove_happyPath() {
        // given
        var driverId = UUID.fromString("a1aaba2a-3f07-4d62-b47b-09d5f0708815");
        var order = orderDbHelper.save();

        // when
        service.createOffer(driverId, order.getId());

        // Отладка: смотрим, что записалось
        var beforeRemove = redisTemplate.opsForZSet().range(ACTIVE_OFFERS_KEY, 0, -1);
        System.out.println("Before remove: " + beforeRemove);
        assertEquals(1, beforeRemove.size());
        service.removeActiveOffer(order.getId(), driverId);

        // then
        var set = redisTemplate.opsForZSet().range(ACTIVE_OFFERS_KEY, 0, -1);
        System.out.println("After remove: " + set);
        assertEquals(0, set.size());
    }
}
