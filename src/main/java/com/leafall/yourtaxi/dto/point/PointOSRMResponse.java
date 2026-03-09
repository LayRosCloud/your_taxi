package com.leafall.yourtaxi.dto.point;

import lombok.Data;

import java.util.List;

@Data
public class PointOSRMResponse {
    private String code;
    private List<PointOSRMRoute> routes;
}
