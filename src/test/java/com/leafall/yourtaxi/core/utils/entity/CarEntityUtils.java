package com.leafall.yourtaxi.core.utils.entity;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.utils.TimeUtils;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public abstract class CarEntityUtils {

    public static CarEntity generate() {
        var generated = new CarEntity();
        var color = faker.color().name();
        var mark = faker.name().title();
        var number = faker.address().zipCode();
        generated.setColor(color.substring(0, Math.min(color.length(), 40)));
        generated.setMark(mark.substring(0, Math.min(mark.length(), 40)));
        generated.setNumber(number.substring(0, Math.min(number.length(), 15)));
        generated.setCreatedAt(TimeUtils.getCurrentTimeFromUTC());
        return generated;
    }
}
