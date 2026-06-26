package com.leafall.yourtaxi.core.utils.dto;

import com.leafall.yourtaxi.dto.variable.VariableUpdateDto;

import java.util.UUID;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;

public abstract class VariableDtoUtils {
    public static VariableUpdateDto generate(UUID id) {
        var variable = new VariableUpdateDto();
        variable.setId(id);
        variable.setValue(faker.random().nextInt(0 ,10).toString());
        variable.setDescription(faker.name().fullName());
        return variable;
    }


}
