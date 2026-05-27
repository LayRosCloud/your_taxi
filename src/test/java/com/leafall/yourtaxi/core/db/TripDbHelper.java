package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.core.utils.entity.TripEntityUtils;
import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TripDbHelper {
    private final TripRepository tripRepository;

    public TripEntity save(UserEntity user, CarEntity car) {
        var entity = TripEntityUtils.generate(car, user);
        return tripRepository.save(entity);
    }

    public Optional<TripEntity> findById(UUID id) {
        return tripRepository.findById(id);
    }
}
