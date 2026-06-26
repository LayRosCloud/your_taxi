package com.leafall.yourtaxi.core.utils.equals;

import com.leafall.yourtaxi.dto.variable.VariableResponseDto;
import com.leafall.yourtaxi.dto.variable.VariableUpdateDto;
import com.leafall.yourtaxi.entity.VariableEntity;
import com.leafall.yourtaxi.entity.enums.VariableType;

import static org.junit.jupiter.api.Assertions.*;

public final class VariableEqualsUtils {

    public static void equals(VariableEntity entity, VariableResponseDto dto) {
        assertNotNull(entity);
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getType(), dto.getType());
        assertEquals(entity.getValue(), dto.getValue());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getKey(), dto.getKey());
    }

    public static void equals(VariableResponseDto entity, VariableUpdateDto dto) {
        assertNotNull(entity);
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        if (entity.getType().equals(VariableType.NUMBER)) {
            assertEquals(entity.getValue(), Double.parseDouble(dto.getValue()));
        } else {
            assertEquals(entity.getValue(),dto.getValue());
        }
        assertEquals(entity.getDescription(), dto.getDescription());
    }
}
