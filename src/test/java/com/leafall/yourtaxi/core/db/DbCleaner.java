package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.repository.*;
import com.leafall.yourtaxi.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbCleaner {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TokenRepository tokenRepository;
    private final TripRepository tripRepository;
    private final CarRepository carRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final CodeRepository codeRepository;

    public void clear() {
        tokenRepository.deleteAll();
        codeRepository.deleteAll();
        userRepository.deleteAll();
        orderHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        tripRepository.deleteAll();
        carRepository.deleteAll();
    }
}
