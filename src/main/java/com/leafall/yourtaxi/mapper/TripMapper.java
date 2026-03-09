package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.trip.TripResponseDto;
import com.leafall.yourtaxi.entity.TripEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CarMapper.class})
public interface TripMapper {
    TripResponseDto mapToDto(TripEntity trip);
}
