package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.trip.TripEndDto;
import com.leafall.yourtaxi.dto.trip.TripResponseDto;
import com.leafall.yourtaxi.dto.trip.TripStartDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseBadRequest;
import com.leafall.yourtaxi.exception.annotation.ApiResponseForbidden;
import com.leafall.yourtaxi.exception.annotation.ApiResponseNotFound;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Сессии исполнителей")
public class TripController {

    private final TripService tripService;


    @GetMapping("/v1/trips/active")
    @Operation(
            summary = "Получить активную сессию",
            description = "Получить активную сессию, она нужна, чтобы принимать заказы"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponseForbidden
    @ApiResponse(description = "Получена активная сессия", responseCode = "200")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<TripResponseDto> getActiveSession() {
       var trip = tripService.findYourActiveTrip();
       return new ResponseEntity<>(trip, HttpStatus.OK);
    }

    @PostMapping("/v1/trips/start")
    @Operation(
            summary = "Создать сессию",
            description = "Создана рабочая сессия в рамках которой будет работать исполнитель"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponseForbidden
    @ApiResponse(description = "Создана новая сессия", responseCode = "201")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TripResponseDto> startTrip(@RequestBody @Valid TripStartDto dto) {
        var trip = tripService.startTrip(dto);
        return new ResponseEntity<>(trip, HttpStatus.CREATED);
    }

    @PostMapping("/v1/trips/stop")
    @Operation(
            summary = "Остановить сессию",
            description = "Будет остановлена рабочая сессия машина становится доступна другим"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponseForbidden
    @ApiResponse(description = "Остановлена сессия с машиной", responseCode = "200")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<TripResponseDto> stopTrip(@RequestBody @Valid TripEndDto dto) {
        var trip = tripService.stopTrip(dto);
        return new ResponseEntity<>(trip, HttpStatus.OK);
    }
}
