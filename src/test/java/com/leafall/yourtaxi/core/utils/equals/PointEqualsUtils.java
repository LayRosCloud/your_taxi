package com.leafall.yourtaxi.core.utils.equals;

import com.leafall.yourtaxi.dto.point.PointCreateDto;
import com.leafall.yourtaxi.dto.point.PointResponseDto;
import static org.junit.jupiter.api.Assertions.*;
public abstract class PointEqualsUtils {

    public static void equals(PointCreateDto dto, PointResponseDto dto1) {
        assertNotNull(dto);
        assertNotNull(dto1);
        assertEquals(dto.getLongitude(), dto1.getLongitude());
        assertEquals(dto.getLatitude(), dto1.getLatitude());
        assertEquals(dto.getName(), dto1.getName());
    }
}
