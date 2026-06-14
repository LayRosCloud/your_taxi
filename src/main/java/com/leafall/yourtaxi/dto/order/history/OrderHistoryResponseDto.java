package com.leafall.yourtaxi.dto.order.history;

import com.leafall.yourtaxi.entity.enums.OrderStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderHistoryResponseDto {
    private UUID id;
    private String message;
    private OrderStatus status;
    private Double longitude;
    private Double latitude;
    private Long createdAt;
}
