package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, Long> {
}
