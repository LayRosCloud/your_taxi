package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, UUID>, JpaSpecificationExecutor<TripEntity> {
    @Query(value = "SELECT t from TripEntity t " +
            "WHERE (t.car = :car OR t.user = :user) AND t.endAt is null " +
            "ORDER BY t.createdAt DESC " +
            "limit 1")
    Optional<TripEntity> findByCarOrUserAndEndAtIsNull(CarEntity car, UserEntity user);
    Optional<TripEntity> findByCarAndUserAndEndAtIsNull(CarEntity car, UserEntity user);
    Optional<TripEntity> findByUserAndEndAtIsNull(UserEntity user);
}
