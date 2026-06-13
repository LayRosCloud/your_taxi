package com.leafall.yourtaxi.dispatch;

import com.leafall.yourtaxi.dto.coordinates.CoordinateResponseDto;
import com.leafall.yourtaxi.dto.coordinates.CoordinateSaveDto;
import com.leafall.yourtaxi.dto.point.PointCreateDto;
import com.leafall.yourtaxi.dto.point.PointOSRMResponse;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.repository.OrderRepository;
import com.leafall.yourtaxi.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.leafall.yourtaxi.dispatch.DriverDispatchService.DRIVER_STATUS;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {
    public static final String DRIVER_COORDS_PREFIX = "taxi:coords:";
    public static final String GEO_KEY = "taxi:geo_index";
    private final GeometryFactory geometryFactory;
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

        var driversIds = results.getContent().stream().map(result -> result.getContent().getName().toString()).toList();

        List<CoordinateResponseDto> activeDrivers = new ArrayList<>(driversIds.size());

        for (String idStr : driversIds) {
            Optional<CoordinateResponseDto> driverOpt = getDriverLocation(UUID.fromString(idStr));
            driverOpt.ifPresent(activeDrivers::add);
        }
        return activeDrivers;
    }

    public CoordinateResponseDto updateDriverLocation(UUID driverId, CoordinateSaveDto dto) {
        if (dto.getLongitude() > 180 || dto.getLongitude() < -180 || dto.getLatitude() > 90 || dto.getLatitude() < -90) {
            log.warn("Пришли плохие координаты");
            throw new BadRequestException();
        }
        String locationKey = DRIVER_COORDS_PREFIX + driverId;
        Map<String, Object> map = new HashMap<>();
        map.put("id", driverId.toString());
        map.put("lat", dto.getLatitude());
        map.put("lon", dto.getLongitude());
        map.put("ang", dto.getAngle());
        map.put("ts", TimeUtils.getCurrentTimeFromUTC());
        redisTemplate.opsForHash().putAll(locationKey, map);
        redisTemplate.expire(locationKey, 10, TimeUnit.SECONDS);
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(dto.getLongitude(), dto.getLatitude()), driverId);
        var result = new CoordinateResponseDto();
        result.setAngle(dto.getAngle());
        result.setLatitude(dto.getLatitude());
        result.setLongitude(dto.getLongitude());
        result.setId(driverId);
        return result;
    }

    public Optional<CoordinateResponseDto> getDriverLocation(UUID driverId) {
        String locationKey = DRIVER_COORDS_PREFIX + driverId;

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(locationKey);

        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        var extractedRedis = extractFromRedisCoordinates(entries);
        if (extractedRedis == null) {
            return Optional.empty();
        }
        var status = redisTemplate.opsForHash().get(DRIVER_STATUS + driverId, "status");
        if (status != null) {
            extractedRedis.setStatus(status.toString());
        }
        return Optional.of(extractedRedis);
    }

    private CoordinateResponseDto extractFromRedisCoordinates(Map<Object, Object> driverData) {
        if (driverData == null) {
            return null;
        }
        if (driverData.get("id") == null) {
            return null;
        }
        var dto = new CoordinateResponseDto();
        dto.setId(UUID.fromString(driverData.get("id").toString()));
        dto.setLongitude(Double.parseDouble(driverData.get("lon").toString()));
        dto.setLatitude(Double.parseDouble(driverData.get("lat").toString()));
        dto.setAngle(Double.parseDouble(driverData.get("ang").toString()));
        Object statusObj = driverData.get("status");
        if (statusObj != null) {
            dto.setStatus(statusObj.toString());
        }
        return dto;
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

    public Geometry mapFromDtoToPoint(CoordinateResponseDto dto) {
        if (dto == null) {
            return null;
        }
        return geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
    }
}
