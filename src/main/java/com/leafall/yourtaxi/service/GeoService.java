package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.coordinates.CoordinateResponseDto;
import com.leafall.yourtaxi.dto.coordinates.CoordinateSaveDto;
import com.leafall.yourtaxi.dto.point.PointCreateDto;
import com.leafall.yourtaxi.dto.point.PointOSRMResponse;
import com.leafall.yourtaxi.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {
    private static final String KEY = "taxi:coords:";
    private static final String GEO_KEY = "taxi:geo_index";

    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${services.osrm.host}")
    private String host;

    private final RestTemplate restTemplate;
    public List<CoordinateResponseDto> getNearbyDrivers(Double lon, Double lat, Double radius) {
        var uuid = UUID.randomUUID();
        log.info("[{}] lon={},lat={},radius={}",uuid, lon, lat, radius);
        var distance = new Distance(radius, Metrics.KILOMETERS);
        var reference = GeoReference.fromCircle(new Circle(new Point(lon, lat), distance));

        var args = RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();
        var results = redisTemplate.opsForGeo()
                .search(GEO_KEY, reference, distance, args);
        log.info("[{}] lon={},lat={},radius={};result={}",uuid, lon, lat, radius, results.getContent());
        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        var driversIds = results.getContent().stream().map(result -> result.getContent().getName()).toList();
        var keys = driversIds.stream().map(id -> KEY + id).toList();
        var rawValues = redisTemplate.opsForValue().multiGet(keys);

        List<CoordinateResponseDto> activeDrivers = new ArrayList<>(driversIds.size());

        for (int i = 0; i < keys.size(); i++) {
            Object value = rawValues.get(i);
            if (value == null) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> driverData = (Map<String, Object>) value;
            var dto = new CoordinateResponseDto();
            dto.setId(UUID.fromString(driverData.get("id").toString()));
            dto.setLongitude(Double.parseDouble(driverData.get("lon").toString()));
            dto.setLatitude(Double.parseDouble(driverData.get("lat").toString()));
            dto.setAngle(Double.parseDouble(driverData.get("ang").toString()));

            activeDrivers.add(dto);
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

    public PointOSRMResponse getDistance(PointCreateDto point1, PointCreateDto point2) {
        var url = String.format("%s/route/v1/driving/%s,%s;%s,%s?overview=false", host, point1.getLongitude(), point1.getLatitude(), point2.getLongitude(), point2.getLatitude());
        var point = restTemplate.getForEntity(url, PointOSRMResponse.class);
        if (!point.getStatusCode().is2xxSuccessful()) {
            log.error("Не удалось сделать запрос на получение дистанции: {} {}", point.getStatusCode(), point.getBody());
            throw new BadRequestException();
        }
        return point.getBody();
    }
}
