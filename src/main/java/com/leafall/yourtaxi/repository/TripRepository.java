package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, UUID> {
    Optional<TripEntity> findByCarOrUserAndEndAtIsNull(CarEntity car, UserEntity user);
    Optional<TripEntity> findByCarAndUserAndEndAtIsNull(CarEntity car, UserEntity user);

    Optional<TripEntity> findByUserAndEndAtIsNull(UserEntity user);
}
