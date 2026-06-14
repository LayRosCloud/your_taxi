package com.leafall.yourtaxi.dto.order.history;

import com.leafall.yourtaxi.entity.enums.OrderStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderHistoryCreateDto {
    private UUID orderId;
    private String message;
    private OrderStatus orderStatus;
    private Long longitude;
    private Long latitude;
}
