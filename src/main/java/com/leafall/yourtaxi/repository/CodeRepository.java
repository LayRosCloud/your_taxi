package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.CodeEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeRepository extends JpaRepository<CodeEntity, Long> {
    Optional<CodeEntity> findByCodeAndUser(String code, UserEntity user);
}
