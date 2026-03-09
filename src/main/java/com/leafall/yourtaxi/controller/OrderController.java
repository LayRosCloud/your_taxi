package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.order.OrderCostDto;
import com.leafall.yourtaxi.dto.order.OrderCreateDto;
import com.leafall.yourtaxi.dto.order.OrderResponseDto;
import com.leafall.yourtaxi.dto.point.PointCostDto;
import com.leafall.yourtaxi.exception.annotation.*;
import com.leafall.yourtaxi.service.OrderService;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Работа с заказами")
public class OrderController {

    private final OrderService service;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/v1/orders/active")
    @Operation(
            summary = "Получить последние активные заказы",
            description = "Получает все активные заказы пользователя (то есть без статуса Completed и Rejected)"
    )
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Получить активные заказы аккаунта", responseCode = "200")
    public ResponseEntity<OrderResponseDto> findAllActiveOrders() {
        var orders = service.findActiveOrder();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("/v1/orders/cost")
    @Operation(
            summary = "Посчитать стоимость поездки и её длительность",
            description = "Считает на основе получаемого тела стоимость и длительность заказа"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Получить стоимость поездки", responseCode = "200")
    public ResponseEntity<PointCostDto> getCost(@RequestBody @Valid OrderCostDto dto) {
        var orders = service.getCostAndDuration(dto);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("/v1/orders")
    @Operation(
            summary = "Создать заказ",
            description = "Создает новый заказ и сразу отправляет на событие `/topic/orders/new` о новом заказе"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Создать заказ", responseCode = "201")
    public ResponseEntity<OrderResponseDto> create(@RequestBody @Valid OrderCreateDto dto) {
        var order = service.create(dto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PatchMapping("/v1/orders/{id}/accept")
    @Operation(
            summary = "Принять заказ от исполнителя",
            description = "Когда исполнитель видит уведомляшку, он принимает"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Подтвердить заказ", responseCode = "200")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<OrderResponseDto> accept(@PathVariable UUID id) {
        var order = service.accept(id);
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/topic/orders/change-status", order);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PatchMapping("/v1/orders/{id}/expect")
    @Operation(
            summary = "Поставить на ожидание от исполнителя",
            description = "Когда исполнитель приезжает он нажимает что ждет клиента"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Заказ в ожидание", responseCode = "200")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<OrderResponseDto> expect(@PathVariable UUID id) {
        var order = service.expectOrder(id);
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/topic/orders/change-status", order);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PatchMapping("/v1/orders/{id}/process")
    @Operation(
            summary = "В процессе встреча от исполнителя",
            description = "Когда заказчик приезжает, он снимает ожидание"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Заказ в процессе", responseCode = "200")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<OrderResponseDto> inProcessOrder(@PathVariable UUID id) {
        var order = service.processOrder(id);
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/topic/orders/change-status", order);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PatchMapping("/v1/orders/{id}/complete")
    @Operation(
            summary = "Завершить встречу от исполнителя",
            description = "После процесса поездки автоматически завершает заказ"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Заказ завершен", responseCode = "200")
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<OrderResponseDto> completeOrder(@PathVariable UUID id) {
        var order = service.completeOrder(id);
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/topic/orders/change-status", order);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @MessageMapping("/v1/orders/cancel/{id}")
    public void cancelOrder(@DestinationVariable UUID id) {
        service.cancel(id);
    }

    @PostMapping("/v1/orders/{id}/reject")
    @Operation(
            summary = "Отменить встречу от заказчика",
            description = "Если заказчик передумал на стадии заказ новый, он может его отменить, далее через диспетчера. Событие приходит всем на `/topic/orders/rejecting`"
    )
    @ApiResponseUnauthorized
    @ApiResponseForbidden
    @ApiResponseConflict
    @ApiResponse(description = "Заказ отменен", responseCode = "200")
    public ResponseEntity<OrderResponseDto> rejectOrder(@PathVariable UUID id) {
        var order = service.reject(id);
        messagingTemplate.convertAndSend("/topic/orders/rejecting", order);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
