package com.leafall.yourtaxi.dto.order;

import lombok.Data;

@Data
public class OrderResponseAndOrderWaiting {
    private OrderResponseDto order;
    private OrderRedisWaitingDto dto;
}
