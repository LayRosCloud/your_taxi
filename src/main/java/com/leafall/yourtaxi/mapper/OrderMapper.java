package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.order.OrderResponseDto;
import com.leafall.yourtaxi.dto.order.OrderResponseWithDurationDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PointMapper.class, TripMapper.class, UserMapper.class})
public interface OrderMapper {
    OrderResponseDto mapToDto(OrderEntity entity);
    @Mapping(target = "durationInSeconds", ignore = true)
    OrderResponseWithDurationDto mapToDtoDuration(OrderEntity entity);
}
