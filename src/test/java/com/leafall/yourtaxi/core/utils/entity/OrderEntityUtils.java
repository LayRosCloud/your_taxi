package com.leafall.yourtaxi.core.utils.entity;

import com.leafall.yourtaxi.entity.*;
import com.leafall.yourtaxi.entity.enums.OrderPaymentType;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.UUID;
import java.util.concurrent.Executor;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public class OrderEntityUtils {
    static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static OrderEntity generate(UserEntity user, @Nullable TripEntity executor) {
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setStatus(OrderStatus.NEW);
        order.setExecutor(executor);
        order.setPrice(faker.number().randomDouble(2, 10, 1000));
        order.setPaymentType(OrderPaymentType.CARD);
        order.setIsBigDistance(false);
        return order;
    }

    public static PointEntity generatePoint(Integer index, OrderEntity order) {
        var point = new PointEntity();
        var coordinate = new Coordinate(faker.number().randomDouble(2, -89, 89), faker.number().randomDouble(2, -89, 89));
        var cord = GEOMETRY_FACTORY.createPoint(coordinate);
        cord.setSRID(4326);
        var key = new PointKey();
        key.setIndex(index);
        key.setOrderId(order.getId());
        point.setId(key);
        point.setPoint(cord);
        point.setOrder(order);
        point.setName(faker.address().streetName());
        return point;
    }
}
