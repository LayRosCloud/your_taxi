package com.leafall.yourtaxi.dto.point;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PointOSRMRoute {
    private Double distance;
    private Double duration;

    @JsonProperty("weight_name")
    private String weightName;
    private Double weight;
}
