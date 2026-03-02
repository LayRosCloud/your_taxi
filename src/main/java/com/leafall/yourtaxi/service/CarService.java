package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.car.CarCreateDto;
import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.car.CarUpdateDto;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.CarMapper;
import com.leafall.yourtaxi.repository.CarRepository;
import com.leafall.yourtaxi.utils.TimeUtils;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
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
    public CarResponseDto findById(Long id) {
        var car = carRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("car.error.not-found"));
        return carMapper.mapToDto(car);
    }

    @Transactional
    public CarResponseDto create(CarCreateDto dto) {
        var toSave = carMapper.mapToEntity(dto);
        var car = carRepository.save(toSave);
        return carMapper.mapToDto(car);
    }

    @Transactional
    public CarResponseDto update(CarUpdateDto dto) {
        var car = carRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("car.error.not-found"));
        carMapper.update(car, dto);
        var newCar = carRepository.save(car);
        return carMapper.mapToDto(newCar);
    }

    @Transactional
    public void delete(Long id) {
        var car = carRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("car.error.not-found"));
        car.setDeletedAt(TimeUtils.getCurrentTimeFromUTC());
        carRepository.save(car);
    }
}
