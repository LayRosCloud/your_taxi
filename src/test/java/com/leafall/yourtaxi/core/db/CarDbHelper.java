package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.core.utils.entity.CarEntityUtils;
import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarDbHelper {
    private final CarRepository carRepository;

    public CarEntity save() {
        var entity = CarEntityUtils.generate();
        return carRepository.save(entity);
    }
}
