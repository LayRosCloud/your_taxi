package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dispatch.DriverDispatchService;
import com.leafall.yourtaxi.dto.trip.TripEndDto;
import com.leafall.yourtaxi.dto.trip.TripResponseDto;
import com.leafall.yourtaxi.dto.trip.TripStartDto;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.enums.TripStatus;
import com.leafall.yourtaxi.exception.ConflictException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.TripMapper;
import com.leafall.yourtaxi.repository.CarRepository;
import com.leafall.yourtaxi.repository.OrderRepository;
import com.leafall.yourtaxi.repository.TripRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import com.leafall.yourtaxi.repository.specification.TripSpecification;
import com.leafall.yourtaxi.utils.SecurityUtils;
import com.leafall.yourtaxi.utils.TimeUtils;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import com.leafall.yourtaxi.utils.request.TripRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final DriverDispatchService dispatchService;
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

    @Transactional(readOnly = true)
    public PaginationResponse<TripResponseDto> findAll(TripRequestDto query) {
        var tripSpecification = TripSpecification.betweenDates(query.getDateFrom() != null ? Date.valueOf(query.getDateFrom()) : null, query.getDateTo() != null ?  Date.valueOf(query.getDateTo()) : null)
                .and(TripSpecification.search(query.getSearch()))
                .and(TripSpecification.equalsEmployeeId(query.getEmployeeId()));
        var params = new PaginationParams(query.getLimit(), query.getPage());
        var pagination = params.getPageable(false, "createdAt");
        var trips = tripRepository.findAll(tripSpecification, pagination);
        var dtoResult = trips.stream().map(tripMapper::mapToDto).toList();
        return new PaginationResponse<>(dtoResult, new PaginationCursor(params, trips.getTotalElements()));
    }

    @Transactional
    public TripResponseDto startTrip(TripStartDto dto, UUID userId) {
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
        trip.setStatus(TripStatus.FREE);
        var newTrip = tripRepository.save(trip);
        var orders = orderRepository.findAllByPlannerDriverAndScheduledStartTimeBetween(user, TimeUtils.getStartOfDayUTC(), TimeUtils.getEndOfDayUTC());
        if (orders.size() > 0) {
            for (var order: orders) {
                order.setExecutor(trip);
            }
            orderRepository.saveAll(orders);
        }
        dispatchService.addToQueue(userId);
        return tripMapper.mapToDto(newTrip);
    }

    @Transactional
    public TripResponseDto stopTrip(TripEndDto dto, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new NotFoundException("car.error.not-found"));
        var trip = tripRepository.findByCarAndUserAndEndAtIsNull(car, user)
                .orElseThrow(() -> new NotFoundException("trip.error.not-found"));
        dispatchService.removeFromQueue(userId);
        trip.setEndAt(TimeUtils.getCurrentTimeFromUTC());
        var savedTrip = tripRepository.save(trip);
        return tripMapper.mapToDto(savedTrip);
    }


}
