package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.dto.point.PointCreateDto;
import lombok.Data;

@Data
public class OrderCreateDto {
    private PointCreateDto from;
    private PointCreateDto to;
}
