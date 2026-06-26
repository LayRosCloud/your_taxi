package com.leafall.yourtaxi.core.utils.entity;

import com.leafall.yourtaxi.dto.variable.VariableUpdateDto;
import com.leafall.yourtaxi.entity.VariableEntity;
import com.leafall.yourtaxi.entity.enums.VariableType;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public abstract class VariableEntityUtils {

    public static VariableEntity generate() {
        var variable = new VariableEntity();
        variable.setValue(faker.random().nextInt(0 ,10).toString());
        variable.setDescription(faker.name().fullName());
        variable.setType(VariableType.STRING);
        variable.setKey(faker.name().username());
        return variable;
    }
}
