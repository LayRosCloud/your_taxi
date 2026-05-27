package com.leafall.yourtaxi.core.utils.dto;

import com.leafall.yourtaxi.dto.point.PointCreateDto;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public abstract class PointCreateDtoUtils {

    public static PointCreateDto generate(double lat, double lon) {
        var dto = new PointCreateDto();
        dto.setName(faker.address().city());
        dto.setLatitude(lat);
        dto.setLongitude(lon);
        return dto;
    }
}
