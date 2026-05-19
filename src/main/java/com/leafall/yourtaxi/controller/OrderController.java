package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.order.*;
import com.leafall.yourtaxi.dto.point.PointCostDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.exception.annotation.*;
import com.leafall.yourtaxi.service.OrderService;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.leafall.yourtaxi.utils.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Работа с заказами")
@Slf4j
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
    public ResponseEntity<OrderResponseWithDurationDto> findAllActiveOrders() {
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
        log.info("Получение цен между точками: {}", dto);
        var orders = service.getCostAndDuration(dto);
        log.info("Цены получены: {}", orders);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("/v1/orders")
    @Operation(
            summary = "Создать заказ",
            description = "Создает новый заказ и сразу отправляет на событие `/user/queue/orders/new` о новом заказе"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Создать заказ", responseCode = "201")
    public ResponseEntity<OrderResponseDto> create(@RequestBody @Valid OrderCreateDto dto) {
        log.info("Начало создания заказа: from={}, to={} currentUserId={}", dto.getFrom(), dto.getTo(), getCurrentUserId());
        var order = service.create(dto);
        log.info("Заказ создан: id={}", order.getId());
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
        log.info("Начало принятия заказа: id={}, currentUserId={}", id, getCurrentUserId());
        var order = service.accept(id);
        log.info("Заказ {} принят в исполнение исполнителем {}.", id, getCurrentUserId());
        log.debug("[/queue/orders/change-status] Начало отправки уведомления пользователю {}", order.getUser().getId().toString());
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/queue/orders/change-status", order);
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
        log.info("Начало ожидания заказа: id={}, currentUserId={}", id, getCurrentUserId());
        var order = service.expectOrder(id);
        log.info("Заказ {} принят в ожидание исполнителем {}.", id, getCurrentUserId());
        log.debug("[/queue/orders/change-status] Начало отправки уведомления пользователю {}", order.getUser().getId().toString());
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/queue/orders/change-status", order);
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
        log.info("Начало выполнения заказа: id={}, currentUserId={}", id, getCurrentUserId());
        var order = service.processOrder(id);
        log.info("Заказ {} принят в работу исполнителем {}.", id, getCurrentUserId());
        log.debug("[/queue/orders/change-status] Начало отправки уведомления пользователю {}", order.getUser().getId().toString());
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/queue/orders/change-status", order);
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
        log.info("Начало завершения заказа: id={}, currentUserId={}", id, getCurrentUserId());
        var order = service.completeOrder(id);
        log.info("Заказ {} завершен исполнителем {}.", id, getCurrentUserId());
        log.debug("[/queue/orders/change-status] Начало отправки уведомления пользователю {}", order.getUser().getId().toString());
        messagingTemplate.convertAndSendToUser(order.getUser().getId().toString(), "/queue/orders/change-status", order);
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
        log.info("Начало отмены заказа: id={}, currentUserId={}", id, getCurrentUserId());
        var order = service.reject(id);
        log.info("Заказ {} отменен заказчиком {}.", id, getCurrentUserId());
        log.debug("[/topic/orders/rejecting] Начало отправки уведомления всем ");
        messagingTemplate.convertAndSend("/topic/orders/rejecting", order);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
