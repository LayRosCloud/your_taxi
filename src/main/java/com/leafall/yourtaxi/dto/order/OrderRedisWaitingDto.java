package com.leafall.yourtaxi.dto.order;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class OrderRedisWaitingDto {
    private UUID id;
    private Double longitude;
    private Double latitude;
    private Set<String> ids;
}
