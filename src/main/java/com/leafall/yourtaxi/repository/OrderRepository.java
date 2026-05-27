package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {
    List<OrderEntity> findAllByUserAndStatusNotIn(UserEntity user, Collection<OrderStatus> status);
    List<OrderEntity> findAllByExecutorAndStatusNotIn(TripEntity executor, Collection<OrderStatus> status);
    List<OrderEntity> findAllByPlannerDriverAndScheduledStartTimeBetween(UserEntity plannerDriver, Long scheduledStartTime, Long scheduledStartTime2);
    @Query("SELECT o FROM OrderEntity o JOIN o.executor e WHERE e.id = :executorId")
    Optional<OrderEntity> findByEmployeeId(@Param("executorId") UUID executorId);
}
