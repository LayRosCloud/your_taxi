package com.leafall.yourtaxi.core.utils.entity;

import com.leafall.yourtaxi.entity.CarEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.utils.TimeUtils;

public abstract class TripEntityUtils {

    public static TripEntity generate(CarEntity car, UserEntity user) {
        var trip = new TripEntity();
        trip.setCar(car);
        trip.setUser(user);
        trip.setCreatedAt(TimeUtils.getCurrentTimeFromUTC());
        return trip;
    }
}
