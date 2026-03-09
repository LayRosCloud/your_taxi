package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.point.PointCreateDto;
import com.leafall.yourtaxi.dto.point.PointResponseDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.PointEntity;
import com.leafall.yourtaxi.entity.PointKey;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PointMapper {
    GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    default PointEntity mapToEntity(PointCreateDto dto, OrderEntity entity, int index) {
        var point = new PointEntity();
        var key = new PointKey();
        key.setIndex(index);
        key.setOrderId(entity.getId());
        point.setId(key);
        point.setName(dto.getName());
        point.setOrder(entity);
        var coordinate = new Coordinate(dto.getLongitude(), dto.getLatitude());
        var coordPoint = GEOMETRY_FACTORY.createPoint(coordinate);
        coordPoint.setSRID(4326);
        point.setPoint(coordPoint);
        return point;
    }

    default PointResponseDto mapToDto(PointEntity point) {
        var pointResponseDto = new PointResponseDto();
        pointResponseDto.setIndex(point.getIndex());
        pointResponseDto.setLongitude(point.getPoint().getCoordinate().x);
        pointResponseDto.setLatitude(point.getPoint().getCoordinate().y);
        pointResponseDto.setName(point.getName());
        return pointResponseDto;
    }
}
