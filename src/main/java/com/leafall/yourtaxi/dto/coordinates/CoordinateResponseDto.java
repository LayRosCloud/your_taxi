package com.leafall.yourtaxi.dto.coordinates;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class CoordinateResponseDto {
    private UUID id;
    private Double longitude;
    private Double latitude;
    private Double angle;
}
