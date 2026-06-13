package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.coordinates.CoordinateResponseDto;
import com.leafall.yourtaxi.dto.coordinates.CoordinateSaveDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.dispatch.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.leafall.yourtaxi.dispatch.OrderAssignmentService.MAX_RADIUS_SEARCH;

@RestController
@RequiredArgsConstructor
@MessageMapping("/v1/coords")
@Tag(name = "Coords", description = "Текущее местоположение")
@Slf4j
public class GeoController {
    private final GeoService geoService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/v1/coords")
    @Operation(
            summary = "Актуальное положение таксистов",
            description = "Получить положение таксистов в определенном радиусе"
    )
    @ApiResponse(description = "Координаты получены", responseCode = "200")
    @ApiResponseUnauthorized
    public ResponseEntity<List<CoordinateResponseDto>> getNearbyCoords(@RequestParam @NotNull Double lon,
                                                                       @RequestParam @NotNull Double lat,
                                                                       @RequestParam Double radius) {
        if (radius == null) {
            radius = MAX_RADIUS_SEARCH;
        }
        log.info("Начала получения координат lon={} lat={} radius={}", lon, lat, radius);
        var geos = geoService.getNearbyDrivers(lon, lat, radius);
        log.info("Получено {} исполнителей.", geos.size());
        return new ResponseEntity<>(geos, HttpStatus.OK);
    }

    @MessageMapping("/set/driver.location")
    @SendToUser("/queue/errors")
    public void setCoordinate(CoordinateSaveDto dto, SimpMessageHeaderAccessor headerAccessor) {
        UUID driverId = UUID.fromString((String) headerAccessor.getSessionAttributes().get("DRIVER_ID"));
        log.info("Установка новой геоточки {} для пользователя {}", dto, driverId);
        var result = geoService.updateDriverLocation(driverId, dto);
        log.info("Точка установлена для пользователя {}. И начало отправки всем /topic/set/position", driverId);
        messagingTemplate.convertAndSend("/topic/set/position", result);

    }
}
