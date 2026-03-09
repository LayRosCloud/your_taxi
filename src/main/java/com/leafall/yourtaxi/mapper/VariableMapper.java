package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.variable.VariableResponseDto;
import com.leafall.yourtaxi.entity.VariableEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VariableMapper {
    default VariableResponseDto mapToDto(VariableEntity variable) {
        if ( variable == null ) {
            return null;
        }
        var response = new VariableResponseDto();
        response.setDescription(variable.getDescription());
        response.setId(variable.getId());
        response.setKey(variable.getKey());
        response.setType(variable.getType());
        switch (variable.getType()) {
            case NUMBER -> response.setValue(Double.parseDouble(variable.getValue()));
            case STRING -> response.setValue(variable.getValue());
            case BOOLEAN -> response.setValue(Boolean.parseBoolean(variable.getValue()));
        }
        return response;
    }
}
