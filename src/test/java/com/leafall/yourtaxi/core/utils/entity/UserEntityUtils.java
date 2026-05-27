package com.leafall.yourtaxi.core.utils.entity;

import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public abstract class UserEntityUtils {

    public static UserEntity generateUser(UserRole role) {
        var generated = new UserEntity();
        generated.setFullName(faker.name().fullName());
        generated.setEmail(faker.internet().emailAddress());
        generated.setRole(role);
        generated.setPassword(faker.internet().password());
        generated.setIsActive(true);
        return generated;
    }
}
