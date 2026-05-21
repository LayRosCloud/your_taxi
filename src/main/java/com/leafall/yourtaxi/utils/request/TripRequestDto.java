package com.leafall.yourtaxi.utils.request;

import lombok.Data;

import java.util.UUID;

@Data
public class TripRequestDto {
    private String search;
    private String dateFrom;
    private String dateTo;
    private UUID employeeId;
    private Integer limit = 10;
    private Integer page = 0;
}
