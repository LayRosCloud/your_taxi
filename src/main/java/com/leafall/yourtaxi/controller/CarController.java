package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.car.CarCreateDto;
import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.car.CarUpdateDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseBadRequest;
import com.leafall.yourtaxi.exception.annotation.ApiResponseForbidden;
import com.leafall.yourtaxi.exception.annotation.ApiResponseNotFound;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.CarService;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Cars", description = "Машины (автопарк)")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping("/v1/cars")
    @Operation(
            summary = "Получить машины (автопарк)",
            description = "Получить машины с учетом пагинации"
    )
    @ApiResponseUnauthorized
    @ApiResponseForbidden
    @ApiResponse(description = "Получены автомобили", responseCode = "200")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'DISPATCHER')")
    public ResponseEntity<PaginationResponse<CarResponseDto>> findAll(@ParameterObject PaginationParams params) {
        var response = carService.findAll(params);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/v1/cars/available")
    @Operation(
            summary = "Получить доступные машины (автопарк)",
            description = "Получить машины с учетом пагинации"
    )
    @ApiResponseUnauthorized
    @ApiResponse(description = "Получены доступные автомобили", responseCode = "200")
    @ApiResponseForbidden
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'DISPATCHER')")
    public ResponseEntity<PaginationResponse<CarResponseDto>> findAllAvailable(@ParameterObject PaginationParams params) {
        var response = carService.findAllAvailable(params);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/v1/cars/{id}")
    @Operation(
            summary = "Получить машину"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Получен автомобиль", responseCode = "200")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'DISPATCHER')")
    public ResponseEntity<CarResponseDto> findById(@PathVariable @Min(0) Long id) {
        var response = carService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/v1/cars")
    @Operation(
            summary = "Создать машину",
            description = "Создать машину (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponse(description = "Создан автомобиль", responseCode = "201")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<CarResponseDto> create(@RequestBody @Valid CarCreateDto dto) {
        var response = carService.create(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/v1/cars")
    @Operation(
            summary = "Обновить машину",
            description = "Обновить машину (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Обновлен автомобиль", responseCode = "200")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<CarResponseDto> update(@RequestBody @Valid CarUpdateDto dto) {
        var response = carService.update(dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/v1/cars/{id}")
    @Operation(
            summary = "Удалить машину (мягкое удаление)",
            description = "Удалить машину (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Удален автомобиль", responseCode = "204")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<Void> delete(@PathVariable @Min(0) Long id) {
        carService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
