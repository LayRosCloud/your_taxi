package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.order.history.OrderHistoryResponseDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseBadRequest;
import com.leafall.yourtaxi.exception.annotation.ApiResponseForbidden;
import com.leafall.yourtaxi.exception.annotation.ApiResponseNotFound;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.OrderHistoryService;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Order History")
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryController {
    private final OrderHistoryService orderHistoryService;

    @GetMapping("/v1/orders/{id}/histories")
    @Operation(
            summary = "Получить историю по заказу",
            description = "Получить историю по заказу"
    )
    @ApiResponse(description = "Получена история", responseCode = "200")
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseForbidden
    @ApiResponseNotFound
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<PaginationResponse<OrderHistoryResponseDto>> findAll(@PathVariable UUID id, @ParameterObject PaginationParams params) {
        log.info("Начало получение историй {}: {}", id, params);
        var result = orderHistoryService.findAll(id, params);
        log.info("Истории получены: {}", result.cursor().getTotal());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/v1/orders/histories/{id}")
    @Operation(
            summary = "Получить историю по id",
            description = "Получить историю по id"
    )
    @ApiResponse(description = "Получена история", responseCode = "200")
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseForbidden
    @ApiResponseNotFound
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<OrderHistoryResponseDto> findById(@PathVariable UUID id) {
        log.info("Начало получение 1 истории {}", id);
        var result = orderHistoryService.findById(id);
        log.info("История получена: {}", result.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
