package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.coordinates.CoordinateResponseDto;
import com.leafall.yourtaxi.dto.coordinates.CoordinateSaveDto;
import com.leafall.yourtaxi.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GeoService {
    private static final String KEY = "taxi:coords:";
    private static final String GEO_KEY = "taxi:geo_index";

    private final RedisTemplate<String, Object> redisTemplate;

    public List<Map<String, Object>> getNearbyDrivers(Double lon, Double lat, Double radius) {
        var circle = new Circle(new Point(lon, lat), new Distance(radius, Metrics.KILOMETERS));
        var results =
                redisTemplate.opsForGeo().search(GEO_KEY, circle);
        System.out.println(results.getContent());
        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        var driversIds = results.getContent().stream().map(result -> result.getContent().getName()).toList();
        System.out.println(driversIds);
        var keys = driversIds.stream().map(id -> KEY + id).toList();
        System.out.println(keys);
        var rawValues = redisTemplate.opsForValue().multiGet(keys);

        List<Map<String, Object>> activeDrivers = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++) {
            Object value = rawValues.get(i);

            if (value != null) {
                Map<String, Object> driverData = (Map<String, Object>) value;
                activeDrivers.add(driverData);
            }
        }
        return activeDrivers;
    }

    public CoordinateResponseDto updateDriverLocation(UUID driverId, CoordinateSaveDto dto) {
        if (dto.getLongitude() > 180 || dto.getLongitude() < -180 || dto.getLatitude() > 90 || dto.getLatitude() < -90) {
            throw new BadRequestException();
        }
        String locationKey = KEY + driverId;

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("id", driverId);
        locationData.put("lat", dto.getLatitude());
        locationData.put("lon", dto.getLongitude());
        locationData.put("ang", dto.getAngle());
        locationData.put("ts", System.currentTimeMillis());
        redisTemplate.opsForValue().set(locationKey, locationData, 10, TimeUnit.SECONDS);

        redisTemplate.opsForGeo().add(GEO_KEY, new Point(dto.getLongitude(), dto.getLatitude()), driverId);
        var result = new CoordinateResponseDto();
        result.setAngle(dto.getAngle());
        result.setLatitude(dto.getLatitude());
        result.setLongitude(dto.getLongitude());
        result.setId(driverId);
        return result;
    }
}
