package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.order.OrderFullResponseDto;
import com.leafall.yourtaxi.dto.order.OrderResponseDto;
import com.leafall.yourtaxi.dto.order.OrderResponseWithDurationDto;
import com.leafall.yourtaxi.dto.point.PointCostDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.OrderPaymentType;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PointMapper.class, TripMapper.class, UserMapper.class})
public interface OrderMapper {
    OrderResponseDto mapToDto(OrderEntity entity);
    OrderFullResponseDto mapToFullDto(OrderEntity entity);
    @Mapping(target = "durationInSeconds", ignore = true)
    OrderResponseWithDurationDto mapToDtoDuration(OrderEntity entity);

    default OrderEntity mapToEntity(PointCostDto dto, OrderPaymentType paymentType, UserEntity user, OrderStatus status) {
        var orderEntity = new OrderEntity();
        orderEntity.setPrice(dto.getPrice());
        orderEntity.setIsBigDistance(dto.getIsBigDistance());
        orderEntity.setDuration(dto.getDurationInSeconds());
        orderEntity.setPaymentType(paymentType);
        orderEntity.setUser(user);
        orderEntity.setStatus(OrderStatus.NEW);
        return orderEntity;
    }
}
