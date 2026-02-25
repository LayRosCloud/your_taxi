package com.leafall.yourtaxi.dto.point;

import lombok.Data;

@Data
public class PointResponseDto {
    private Integer index;
    private String name;
    private Integer longitude;
    private Integer latitude;
}
