package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.employee.EmployeeCreateDto;
import com.leafall.yourtaxi.dto.employee.EmployeeUpdateDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.UserInfoEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.UserMapper;
import com.leafall.yourtaxi.repository.UserInfoRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import com.leafall.yourtaxi.utils.TimeUtils;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final EncodingService encodingService;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public PaginationResponse<UserResponseDto> findAllEmployees(PaginationParams params) {
        var pageable = params.getPageable(false, "createdAt");
        var users = userRepository.findAllByRoleAndDeletedAtIsNull(UserRole.EMPLOYEE, pageable);
        var responseList = users.stream().map(mapper::mapToDto).toList();
        var cursor = new PaginationCursor(params, users.getTotalElements());
        return new PaginationResponse<>(responseList, cursor);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        if (user.getDeletedAt() != null) {
            throw new NotFoundException("user.error.not-found");
        }
        return mapper.mapToDto(user);
    }

    @Transactional
    public UserResponseDto create(EmployeeCreateDto dto) {
        if (!dto.getPassword().equals(dto.getRepeatPassword())) {
            throw new BadRequestException("user.error.password-dont-match");
        }
        var employee = mapper.mapToEntity(dto);
        employee.setRole(UserRole.EMPLOYEE);
        employee.setPassword(encodingService.encode(dto.getPassword()));
        var userInfo = new UserInfoEntity();
        userInfo.setPhone(dto.getPhone());
        userInfo.setUser(employee);
        employee.setIsActive(true);
        employee.setInfo(userInfo);
        var user = userRepository.save(employee);

        return mapper.mapToDto(user);
    }

    @Transactional
    public UserResponseDto update(EmployeeUpdateDto dto) {
        var toSave = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        toSave.setEmail(dto.getEmail());
        toSave.getInfo().setPhone(dto.getPhone());
        toSave.setFullName(dto.getFullName());
        var user = userRepository.save(toSave);
        return mapper.mapToDto(user);
    }

    @Transactional
    public UserResponseDto delete(UUID id) {
        var toSave = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        toSave.setDeletedAt(TimeUtils.getCurrentTimeFromUTC());
        var user = userRepository.save(toSave);
        return mapper.mapToDto(user);
    }

    @Transactional
    public void deleteHard(UUID id) {
        var toSave = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        userRepository.delete(toSave);
    }
}
