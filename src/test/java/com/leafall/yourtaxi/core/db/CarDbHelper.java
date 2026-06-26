package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.core.utils.entity.CarEntityUtils;
import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarDbHelper {
    private final CarRepository carRepository;
    private final UserDbHelper userDbHelper;

    public CarEntity save() {
        var entity = CarEntityUtils.generate();
        entity.setUser(userDbHelper.save(UserRole.DISPATCHER));
        return carRepository.save(entity);
    }

    public CarEntity save(UserEntity user) {
        var entity = CarEntityUtils.generate();
        entity.setUser(user);
        return carRepository.save(entity);
    }
}
