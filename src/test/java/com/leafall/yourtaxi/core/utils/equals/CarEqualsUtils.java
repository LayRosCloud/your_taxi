package com.leafall.yourtaxi.core.utils.equals;

import com.leafall.yourtaxi.dto.car.CarCreateDto;
import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.car.CarUpdateDto;
import com.leafall.yourtaxi.entity.CarEntity;

import static org.junit.jupiter.api.Assertions.*;

public final class CarEqualsUtils {

    public static void equals(CarEntity car, CarResponseDto dto) {
        assertEquals(car.getId(), dto.getId());
        assertEquals(car.getMark(), dto.getMark());
        assertEquals(car.getColor(), dto.getColor());
        assertEquals(car.getNumber(), dto.getNumber());
        UserEqualsUtils.equalsUsers(dto.getUser(), car.getUser());
    }

    public static void equals(CarCreateDto car, CarResponseDto dto) {
        assertEquals(car.getMark(), dto.getMark());
        assertEquals(car.getColor(), dto.getColor());
        assertEquals(car.getNumber(), dto.getNumber());
    }

    public static void equals(CarUpdateDto car, CarResponseDto dto) {
        assertEquals(car.getId(), dto.getId());
        assertEquals(car.getMark(), dto.getMark());
        assertEquals(car.getColor(), dto.getColor());
        assertEquals(car.getNumber(), dto.getNumber());
    }
}
