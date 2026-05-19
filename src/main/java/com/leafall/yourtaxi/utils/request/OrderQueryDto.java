package com.leafall.yourtaxi.utils.request;

import com.leafall.yourtaxi.entity.enums.OrderStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderQueryDto {
    private UUID userId;
    private UUID executorId;
    private OrderStatus status;
    private Double priceFrom;
    private Double priceTo;
    private String dateFrom;
    private String dateTo;
    private Boolean isAscending = false;
    private Integer limit = 10;
    private Integer page = 0;
}
