package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, UUID> {
    Long countByReadAtIsNull();
}
