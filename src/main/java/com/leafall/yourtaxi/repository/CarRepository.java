package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.TokenEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, Long> {
    @Query(value = "SELECT c from CarEntity c " +
            "WHERE NOT EXISTS (SELECT 1 FROM TripEntity t " +
            "WHERE t.car = c AND t.endAt IS NULL" +
            ")",
        countQuery = "SELECT COUNT(c) from CarEntity c " +
            "WHERE NOT EXISTS (SELECT 1 FROM TripEntity t " +
            "WHERE t.car = c AND t.endAt IS NULL" +
            ")")
    Page<CarEntity> findAllAvailable(Pageable pageable);
}
