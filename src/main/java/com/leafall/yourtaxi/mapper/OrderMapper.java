package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.order.OrderResponseDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PointMapper.class})
public interface OrderMapper {
    OrderResponseDto mapToDto(OrderEntity entity);
}
