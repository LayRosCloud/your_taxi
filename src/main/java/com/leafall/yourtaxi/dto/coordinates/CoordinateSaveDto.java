package com.leafall.yourtaxi.dto.coordinates;

import lombok.Data;

import java.io.Serializable;

@Data
public class CoordinateSaveDto implements Serializable {
    private Double longitude;
    private Double latitude;
    private Double angle;
}
