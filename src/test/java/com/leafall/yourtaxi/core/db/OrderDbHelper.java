package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.core.utils.entity.OrderEntityUtils;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import com.leafall.yourtaxi.repository.OrderRepository;
import com.leafall.yourtaxi.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderDbHelper {
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final UserDbHelper userDbHelper;
    public OrderEntity save() {
        var generated = OrderEntityUtils.generate(userDbHelper.save(), null);

        var order = orderRepository.save(generated);
        var from = OrderEntityUtils.generatePoint(0, order);
        var to = OrderEntityUtils.generatePoint(1, order);
        var points = pointRepository.saveAll(List.of(from, to));
        order.setPoints(points);
        return order;
    }

    public OrderEntity save(UserEntity user, @Nullable TripEntity trip) {
        var generated = OrderEntityUtils.generate(user, trip);

        var order = orderRepository.save(generated);
        var from = OrderEntityUtils.generatePoint(0, order);
        var to = OrderEntityUtils.generatePoint(1, order);
        var points = pointRepository.saveAll(List.of(from, to));
        order.setPoints(points);
        return order;
    }

    public OrderEntity save(UserEntity user, OrderStatus orderStatus, @Nullable TripEntity trip) {
        var generated = OrderEntityUtils.generate(user, trip);
        generated.setStatus(orderStatus);

        var order = orderRepository.save(generated);
        var from = OrderEntityUtils.generatePoint(0, order);
        var to = OrderEntityUtils.generatePoint(1, order);
        var points = pointRepository.saveAll(List.of(from, to));
        order.setPoints(points);
        return order;
    }
}
