package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    Page<UserEntity> findAllByRoleAndDeletedAtIsNull(UserRole role, Pageable pageable);
}
