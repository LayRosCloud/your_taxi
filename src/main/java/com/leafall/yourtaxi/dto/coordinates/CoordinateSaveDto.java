package com.leafall.yourtaxi.dto.coordinates;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class CoordinateSaveDto implements Serializable {
    private Double longitude;
    private Double latitude;
    private Double angle;
}
