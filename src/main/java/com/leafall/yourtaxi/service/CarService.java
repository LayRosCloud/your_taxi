package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.car.CarCreateDto;
import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.car.CarUpdateDto;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.exception.ForbiddenException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.CarMapper;
import com.leafall.yourtaxi.repository.CarRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import com.leafall.yourtaxi.utils.TimeUtils;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final CarMapper carMapper;

    @Transactional(readOnly = true)
    public PaginationResponse<CarResponseDto> findAll(PaginationParams params) {
        var pageable = params.getPageable(false, "id");
        var carPage = carRepository.findAll(pageable);
        var cursor = new PaginationCursor(params, carPage.getTotalElements());
        var dtoList = carPage.getContent().stream().map(carMapper::mapToDto).toList();
        return new PaginationResponse<>(dtoList, cursor);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<CarResponseDto> findAllAvailable(UUID userId, PaginationParams params) {
        var pageable = params.getPageable(false, "id");
        var carPage = carRepository.findAllAvailable(userId, UserRole.DISPATCHER, pageable);
        var cursor = new PaginationCursor(params, carPage.getTotalElements());
        var dtoList = carPage.getContent().stream().map(carMapper::mapToDto).toList();
        return new PaginationResponse<>(dtoList, cursor);
    }

    @Transactional(readOnly = true)
    public CarResponseDto findLast(UUID userId) {
        var carItem = carRepository.findByTripLast(userId)
                .orElseThrow(() -> new NotFoundException("car.error.not-found"));
        return carMapper.mapToDto(carItem);
    }

    @Transactional(readOnly = true)
    public CarResponseDto findById(Long id) {
        var car = carRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Машина {} не найдена", id);
                    return new NotFoundException("car.error.not-found");
                });
        return carMapper.mapToDto(car);
    }

    @Transactional
    public CarResponseDto create(CarCreateDto dto, UUID userId) {
        var toSave = carMapper.mapToEntity(dto);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь {} не найден", userId);
                    return new NotFoundException("user.error.not-found");
                });
        toSave.setUser(user);
        var car = carRepository.save(toSave);
        return carMapper.mapToDto(car);
    }

    @Transactional
    public CarResponseDto update(CarUpdateDto dto,  UUID userId) {
        var car = carRepository.findById(dto.getId())
                .orElseThrow(() -> {
                    log.info("Машина {} не найдена", dto.getId());
                    return new NotFoundException("car.error.not-found");
                });
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь {} не найден", userId);
                    return new NotFoundException("user.error.not-found");
                });
        if (!user.getId().equals(car.getUser().getId()) && user.getRole().equals(UserRole.EMPLOYEE)) {
            log.info("Сотрудник \"{}\" \"{}\" не является владельцем машины \"{}\". Её владельцем является пользователь id=\"{}\" fullName=\"{}\"",
                    userId, user.getFullName(), dto.getId(), car.getUser().getId(), car.getUser().getFullName());
            throw new ForbiddenException("base.error.forbidden");
        }
        carMapper.update(car, dto);
        var newCar = carRepository.save(car);
        return carMapper.mapToDto(newCar);
    }

    @Transactional
    public void delete(Long id, UUID userId) {
        var car = carRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Машина {} не найдена", id);
                    return new NotFoundException("car.error.not-found");
                });
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь {} не найден", userId);
                    return new NotFoundException("user.error.not-found");
                });
        if (!user.getId().equals(car.getUser().getId()) && user.getRole().equals(UserRole.EMPLOYEE)) {
            log.info("Сотрудник \"{}\" \"{}\" не является владельцем машины \"{}\". Её владельцем является пользователь id=\"{}\" fullName=\"{}\"",
                    userId, user.getFullName(), id, car.getUser().getId(), car.getUser().getFullName());
            throw new ForbiddenException("base.error.forbidden");
        }
        car.setDeletedAt(TimeUtils.getCurrentTimeFromUTC());
        carRepository.save(car);
    }
}
