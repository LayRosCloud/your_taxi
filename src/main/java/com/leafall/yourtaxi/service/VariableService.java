package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.variable.VariableResponseDto;
import com.leafall.yourtaxi.dto.variable.VariableUpdateDto;
import com.leafall.yourtaxi.entity.enums.VariableType;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.VariableMapper;
import com.leafall.yourtaxi.repository.VariableRepository;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class VariableService {

    private final VariableRepository variableRepository;
    private final VariableMapper mapper;

    @Transactional(readOnly = true)
    public PaginationResponse<VariableResponseDto> findAll(PaginationParams params) {
        var pageable = params.getPageable(false, "id");
        var variablePage = variableRepository.findAll(pageable);
        var variables = variablePage.getContent().stream().map(mapper::mapToDto).toList();
        var cursor = new PaginationCursor(params, variablePage.getTotalElements());
        return new PaginationResponse<>(variables, cursor);
    }

    @Transactional(readOnly = true)
    public VariableResponseDto findById(UUID id) {
        var variable = variableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("key.error.not-found"));
        return mapper.mapToDto(variable);
    }

    @Transactional(readOnly = true)
    public VariableResponseDto findByKey(String key) {
        var variable = variableRepository.findByKey(key)
                .orElseThrow(() -> new NotFoundException("key.error.not-found"));
        return mapper.mapToDto(variable);
    }

    @Transactional
    public VariableResponseDto update(VariableUpdateDto dto) {
        var variable = variableRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("key.error.not-found"));
        variable.setValue(dto.getValue());
        variable.setDescription(dto.getDescription());
        try {
            if (variable.getType() == VariableType.NUMBER) {
                Double.parseDouble(dto.getValue());
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("variable.error.format-error");
        }
        var newVariable = variableRepository.save(variable);
        return mapper.mapToDto(newVariable);
    }
}
