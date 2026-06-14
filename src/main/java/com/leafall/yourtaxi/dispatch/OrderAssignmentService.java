package com.leafall.yourtaxi.dispatch;

import com.leafall.yourtaxi.dto.OfferAssignment;
import com.leafall.yourtaxi.dto.order.OrderRedisWaitingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.leafall.yourtaxi.dispatch.SearchService.DRIVER_LOCK_PREFIX;
import static com.leafall.yourtaxi.dispatch.SearchService.ORDERS_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderAssignmentService {

    public static final String ACTIVE_OFFERS_KEY = "taxi:offers:active";
    public static final Double MAX_RADIUS_SEARCH = 10.0;

    private final RedisTemplate<String, Object> redisTemplate;
    private final GeoService geoService;

    private final ObjectMapper objectMapper;

    /**
     * После того как водитель найден и заблокирован - создаем активное предложение
     */
    public void createOffer(UUID driverId, UUID orderId) {
        try {
            long now = System.currentTimeMillis();
            long expiresAt = now + (10 * 1000);

            OfferAssignment offer = new OfferAssignment();
            offer.setOrderId(orderId);
            offer.setDriverId(driverId);
            offer.setCreatedAt(now);
            offer.setExpiresAt(expiresAt);

            String offerJson = objectMapper.writeValueAsString(offer);

            redisTemplate.opsForZSet().add(ACTIVE_OFFERS_KEY, offerJson, expiresAt);

            log.info("Создано предложение: заказ {} водителю {}, истекает через 10 сек",
                    orderId, driverId);

        } catch (Exception e) {
            log.error("Ошибка создания предложения для заказа {} водителю {}", orderId, driverId, e);
        }
    }

    public UUID handleOfferTimeout(UUID orderId, UUID driverId) {
        try {
            log.warn("Таймаут предложения: заказ {} водитель {}", orderId, driverId);

            removeActiveOffer(orderId, driverId);

            redisTemplate.delete(DRIVER_LOCK_PREFIX + driverId);

            String orderKey = ORDERS_KEY + orderId;
            OrderRedisWaitingDto order = (OrderRedisWaitingDto) redisTemplate.opsForValue().get(orderKey);

            if (order == null) {
                log.warn("Заказ {} не найден при обработке таймаута", orderId);
                return null;
            }

            if (!order.getIds().contains(driverId.toString())) {
                log.warn("Заказ {} не содержит заказчика", driverId);
                return null;
            }

            var newDriverId = findDriverForOrder(order.getLongitude(), order.getLatitude(), MAX_RADIUS_SEARCH, order.getId());
            if (newDriverId != null) {
                log.info("Водитель {} найден добавляю делаю ему оффер", newDriverId);
                order.getIds().add(newDriverId.toString());
                redisTemplate.opsForValue().set(String.format("%s%s", ORDERS_KEY, order.getId().toString()), order, 30, TimeUnit.MINUTES);
                createOffer(newDriverId, orderId);
                log.info("Водитель успешно добавлен в заказ {}", orderId);
            } else {
                log.warn("Водитель не найден!");
            }
            return newDriverId;
        } catch (Exception e) {
            log.error("Ошибка обработки таймаута для заказа {}", orderId, e);
            return null;
        }
    }

    public UUID findDriverForOrder(double longitude, double latitude, double radius, UUID orderId) {
        final var maxRetries = 10;
        int attempts = 0;
        var results = geoService.getNearbyDrivers(longitude, latitude, radius);
        if (results == null || results.isEmpty()) {
            log.warn("Нет водителей в радиусе {} км", radius);
            return null;
        }
        var order = getOrderFromRedis(orderId);

        for (var result : results) {
            if (attempts >= maxRetries) {
                break;
            }
            String driverIdStr = result.getId().toString();
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
                createOffer(driverId, orderId);
                log.info("Driver {} found!",
                        driverIdStr);
                return driverId;
            }
            attempts++;
        }

        log.warn("Не удалось подобрать водителя за {} итераций", maxRetries);
        return null;
    }

    public OrderRedisWaitingDto getOrderFromRedis(UUID id) {
        return (OrderRedisWaitingDto) redisTemplate.opsForValue().get(String.format("%s%s", ORDERS_KEY, id));
    }

    public void removeActiveOffer(UUID orderId, UUID driverId) {
        String luaScript =
                "local key = KEYS[1] " +
                        "local orderId = ARGV[1] " +
                        "local driverId = ARGV[2] " +
                        "local members = redis.call('ZRANGE', key, 0, -1) " +
                        "for i, member in ipairs(members) do " +
                        "   local offer = cjson.decode(member) " +
                        "   if tostring(offer.orderId) == orderId and offer.driverId == driverId then " +
                        "       redis.call('ZREM', key, member) " +
                        "       return 1 " +
                        "   end " +
                        "end " +
                        "return 0";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        redisTemplate.execute(script, Collections.singletonList(ACTIVE_OFFERS_KEY),
                orderId.toString(), driverId);
    }
}
