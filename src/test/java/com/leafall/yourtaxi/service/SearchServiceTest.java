package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.dispatch.DriverDispatchService;
import com.leafall.yourtaxi.dispatch.GeoService;
import com.leafall.yourtaxi.dispatch.SearchService;
import com.leafall.yourtaxi.dto.order.OrderRedisWaitingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static com.leafall.yourtaxi.dispatch.DriverDispatchService.DRIVER_STATUS;
import static com.leafall.yourtaxi.dispatch.GeoService.DRIVER_COORDS_PREFIX;
import static com.leafall.yourtaxi.dispatch.GeoService.GEO_KEY;
import static com.leafall.yourtaxi.dispatch.SearchService.DRIVER_LOCK_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

public class SearchServiceTest extends BaseIntegrationTest {
    @Autowired
    private SearchService searchService;
    @Autowired
    private DriverDispatchService dispatchService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private UUID driver1;
    private UUID driver2;
    private UUID driver3;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("taxi:queue:available");

        driver1 = UUID.fromString("92b59163-bbf0-4aee-8b44-bd5b8e210d82");
        driver2 = UUID.fromString("70d34721-9971-46d0-a850-d018f10eecca");
        driver3 = UUID.fromString("9d1d2f77-f420-46a2-80b5-0e74fcedcf36");
        redisTemplate.delete(DRIVER_LOCK_PREFIX + driver1);
        redisTemplate.delete(DRIVER_LOCK_PREFIX + driver2);
        redisTemplate.delete(DRIVER_LOCK_PREFIX + driver3);
        mockDriverLocation(driver1, 55.75, 37.61, "FREE");
        mockDriverLocation(driver2, 55.76, 37.62, "FREE");
        mockDriverLocation(driver3, 55.80, 37.70, "FREE");
        orderId = UUID.fromString("3c901a6b-e05c-4627-8293-d2bbb48d2d44");
        var dto = new OrderRedisWaitingDto();
        dto.setLongitude(37.611);
        dto.setLatitude(55.751);
        dto.setIds(Set.of());
        dto.setId(orderId);
        searchService.addToOrderQueue(dto);
    }

    private void mockDriverLocation(UUID id, double lat, double lon, String status) {
        redisTemplate.opsForGeo().remove(GEO_KEY, id);
        String key = DRIVER_COORDS_PREFIX + id;
        String statusKey = DRIVER_STATUS + id;
        var map = new HashMap<String, Object>();
        map.put("lat", lat);
        map.put("lon", lon);
        map.put("ang", 10);
        map.put("status", status);
        map.put("ts", System.currentTimeMillis());
        map.put("id", id.toString());
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(lon, lat), id);
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.opsForHash().put(statusKey, "status", status);

    }
    @Test
    @DisplayName("Drivers are assigned in turns FIFO")
    void testQueueOrder() {
        // given
        dispatchService.addToQueue(driver1);
        dispatchService.addToQueue(driver2);
        dispatchService.addToQueue(driver3);
        // when
        UUID found = searchService.findDriverForOrder(37.611, 55.751, 1.0, orderId);

        // then
        assertNotNull(found);
        assertEquals(driver1, found, "Первым должен быть предложен Driver1, так как он первый в очереди и близко");

        assertEquals(Boolean.TRUE, redisTemplate.hasKey("taxi:driver:lock:" + driver1));
    }

    @Test
    @DisplayName("If driver is so far")
    void testFarDriverReturnsToTail() {
        // given
        dispatchService.addToQueue(driver3);

        // when
        UUID found = searchService.findDriverForOrder(38.611, 55.751, 1.0, orderId);

        // then
        assertNull(found, "driver is so far, not found");
    }

    @Test
    @DisplayName("Handle reject: Race condition")
    void testRejectHandling() {
        //given
        dispatchService.addToQueue(driver1);
        dispatchService.addToQueue(driver2);

        // when
        UUID firstFound = searchService.findDriverForOrder(37.611, 55.751, 2.0, orderId);

        //then
        assertEquals(driver1, firstFound);

        redisTemplate.delete("taxi:driver:lock:" + driver1);
        dispatchService.addToQueue(driver1);
        var dto = new OrderRedisWaitingDto();
        dto.setLongitude(37.611);
        dto.setLatitude(55.751);
        dto.setIds(Set.of(driver1.toString()));
        dto.setId(orderId);
        searchService.addToOrderQueue(dto);
        UUID secondFound = searchService.findDriverForOrder(37.611, 55.751,2.0, orderId);
        assertEquals(driver2, secondFound, "После отказа первого, вторым должен идти driver2");
    }

    @Test
    @DisplayName("Blocked concurrency locked")
    void testConcurrencyLock() throws InterruptedException {
        //given
        dispatchService.addToQueue(driver1);

        // when
        Thread t1 = new Thread(() -> {
            UUID res = searchService.findDriverForOrder(37.611,55.751,  5.0, orderId);
            if (res != null) System.out.println("Thread 1 got: " + res);
        });

        Thread t2 = new Thread(() -> {
            UUID res = searchService.findDriverForOrder(37.611, 55.751,  5.0, orderId);
            if (res != null) System.out.println("Thread 2 got: " + res);
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        //then
        String locationKey = DRIVER_COORDS_PREFIX + driver1;
        assertEquals(Boolean.TRUE, redisTemplate.hasKey(locationKey));
    }

    @Test
    @DisplayName("Complete order processing and search immediately")
    void testBusyStatusAndChangeFree() {
        //given
        String key = DRIVER_STATUS + driver1;
        dispatchService.addToQueue(driver1);
        redisTemplate.opsForHash().put(key, "status", "BUSY");
        redisTemplate.opsForHash().put(DRIVER_STATUS + driver2, "status", "BUSY");
        redisTemplate.opsForHash().put(DRIVER_STATUS + driver3, "status", "BUSY");
        // when
        UUID firstFound = searchService.findDriverForOrder(37.611, 55.751, 2.0, orderId);
        dispatchService.addToQueue(driver1);
        UUID secondFound = searchService.findDriverForOrder(37.611, 55.751, 2.0, orderId);

        //then
        assertNull(firstFound);
        assertNotNull(secondFound);
    }
}
