package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.trip.TripEndDto;
import com.leafall.yourtaxi.dto.trip.TripResponseDto;
import com.leafall.yourtaxi.dto.trip.TripStartDto;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.exception.ConflictException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.TripMapper;
import com.leafall.yourtaxi.repository.CarRepository;
import com.leafall.yourtaxi.repository.TripRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import com.leafall.yourtaxi.utils.SecurityUtils;
import com.leafall.yourtaxi.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final TripMapper tripMapper;

    @Transactional(readOnly = true)
    public TripResponseDto findYourActiveTrip() {
        var userId = SecurityUtils.getCurrentUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var trip = tripRepository.findByUserAndEndAtIsNull(user)
                .orElseThrow(() -> new NotFoundException("trip.error.not-found"));
        return tripMapper.mapToDto(trip);
    }

    @Transactional
    public TripResponseDto startTrip(TripStartDto dto) {
        var userId = SecurityUtils.getCurrentUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new NotFoundException("car.error.not-found"));
        if (tripRepository.findByCarOrUserAndEndAtIsNull(car, user).isPresent()) {
            throw new ConflictException("trip.error.not-available");
        }
        var trip = new TripEntity();
        trip.setCar(car);
        trip.setUser(user);
        var newTrip = tripRepository.save(trip);
        return tripMapper.mapToDto(newTrip);
    }

    @Transactional
    public TripResponseDto stopTrip(TripEndDto dto) {
        var userId = SecurityUtils.getCurrentUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new NotFoundException("car.error.not-found"));
        var trip = tripRepository.findByCarAndUserAndEndAtIsNull(car, user)
                .orElseThrow(() -> new NotFoundException("trip.error.not-found"));
        trip.setEndAt(TimeUtils.getCurrentTimeFromUTC());
        var savedTrip = tripRepository.save(trip);
        return tripMapper.mapToDto(savedTrip);
    }


}
