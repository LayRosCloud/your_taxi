package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.car.CarCreateDto;
import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.car.CarUpdateDto;
import com.leafall.yourtaxi.entity.CarEntity;
import org.mapstruct.Mapper;

@Mapper
public interface CarMapper {
    CarResponseDto mapToDto(CarEntity car);
    CarEntity mapToEntity(CarCreateDto car);

    default void update(CarEntity entity, CarUpdateDto car) {
        entity.setColor(car.getColor());
        entity.setMark(car.getMark());
        entity.setNumber(car.getNumber());
    }
}
