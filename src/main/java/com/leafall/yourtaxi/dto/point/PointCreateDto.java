package com.leafall.yourtaxi.dto.point;

import lombok.Data;

@Data
public class PointCreateDto {
    private String name;
    private Integer longitude;
    private Integer latitude;
}
