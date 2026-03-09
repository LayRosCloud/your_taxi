package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    Optional<OrderEntity> findByUserAndStatusNotIn(UserEntity user, Collection<OrderStatus> status);
    Optional<OrderEntity> findByExecutorAndStatusNotIn(TripEntity executor, Collection<OrderStatus> status);
}
