package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntity, UUID> {
}
