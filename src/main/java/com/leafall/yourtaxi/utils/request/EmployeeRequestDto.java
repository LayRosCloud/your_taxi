package com.leafall.yourtaxi.utils.request;

import lombok.Data;

@Data
public class EmployeeRequestDto {
    private String search;
    private Integer page = 0;
    private Integer limit = 10;
}
