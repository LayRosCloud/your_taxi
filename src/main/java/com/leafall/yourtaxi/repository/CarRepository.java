package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.TokenEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, Long> {
    @Query(value = "SELECT c from CarEntity c " +
            "WHERE NOT EXISTS (SELECT 1 FROM TripEntity t " +
            "WHERE t.car = c AND t.endAt IS NULL" +
            ") AND (c.user.id = :userId OR c.user.role = :role)",
        countQuery = "SELECT COUNT(c) from CarEntity c " +
            "WHERE NOT EXISTS (SELECT 1 FROM TripEntity t " +
            "WHERE t.car = c AND t.endAt IS NULL" +
            ") AND (c.user.id = :userId OR c.user.role = :role)")
    Page<CarEntity> findAllAvailable(@Param("userId")UUID userId,@Param("role") UserRole role, Pageable pageable);

    @Query(value = "SELECT c from CarEntity c " +
            "WHERE EXISTS (SELECT 1 FROM TripEntity t " +
            "WHERE t.car = c AND t.user.id = :userId" +
            ") AND c.deletedAt IS NULL " +
            "ORDER BY c.createdAt DESC " +
            "LIMIT 1")
    Optional<CarEntity> findByTripLast(@Param("userId") UUID userId);
}
