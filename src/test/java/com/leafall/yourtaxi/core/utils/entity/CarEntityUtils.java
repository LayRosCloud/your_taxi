package com.leafall.yourtaxi.core.utils.entity;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.utils.TimeUtils;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public abstract class CarEntityUtils {

    public static CarEntity generate() {
        var generated = new CarEntity();
        generated.setColor(faker.color().name());
        generated.setMark(faker.name().title());
        generated.setNumber(faker.address().zipCode());
        generated.setCreatedAt(TimeUtils.getCurrentTimeFromUTC());
        return generated;
    }
}
