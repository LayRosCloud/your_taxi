package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.PointEntity;
import com.leafall.yourtaxi.entity.PointKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<PointEntity, PointKey> {
}
