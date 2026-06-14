package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.order.history.OrderHistoryResponseDto;
import com.leafall.yourtaxi.entity.OrderHistoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderHistoryMapper {
    default OrderHistoryResponseDto mapToDto(OrderHistoryEntity entity){
        var orderHistory = new OrderHistoryResponseDto();
        orderHistory.setId(entity.getId());
        orderHistory.setStatus(entity.getStatus());
        if (entity.getPoint() != null) {
            orderHistory.setLongitude(entity.getPoint().getCoordinate().x);
            orderHistory.setLatitude(entity.getPoint().getCoordinate().y);
        }
        orderHistory.setMessage(entity.getMessage());
        orderHistory.setCreatedAt(entity.getCreatedAt());
        return orderHistory;
    }
}
