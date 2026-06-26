package com.leafall.yourtaxi.core.utils.dto;

import com.leafall.yourtaxi.dto.car.CarCreateDto;
import com.leafall.yourtaxi.dto.car.CarUpdateDto;

import java.util.UUID;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public class CarDtoUtils {
    public static CarCreateDto generateDto() {
        var generated = new CarCreateDto();
        generated.setColor(faker.color().name());
        generated.setMark(faker.name().title());
        generated.setNumber(faker.address().zipCode());
        return generated;
    }

    public static CarUpdateDto generateDto(Long id) {
        var generated = new CarUpdateDto();
        generated.setId(id);
        generated.setColor(faker.color().name());
        generated.setMark(faker.name().title());
        generated.setNumber(faker.address().zipCode());
        return generated;
    }
}
