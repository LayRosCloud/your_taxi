package com.leafall.yourtaxi.repository;

import com.leafall.yourtaxi.entity.VariableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VariableRepository extends JpaRepository<VariableEntity, UUID> {

    Optional<VariableEntity> findByKey(String key);
    List<VariableEntity> findAllByKeyIn(Collection<String> key);
}
