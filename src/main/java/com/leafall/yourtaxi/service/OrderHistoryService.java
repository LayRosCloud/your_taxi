package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.order.history.OrderHistoryResponseDto;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.OrderHistoryMapper;
import com.leafall.yourtaxi.repository.OrderHistoryRepository;
import com.leafall.yourtaxi.repository.OrderRepository;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderHistoryMapper mapper;

    @Transactional(readOnly = true)
    public PaginationResponse<OrderHistoryResponseDto> findAll(UUID orderId,PaginationParams params) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("order.error.not-found"));
        var pageable = params.getPageable(false, "createdAt");
        var histories = orderHistoryRepository.findAllByOrder(order, pageable);
        return new PaginationResponse<>(histories.map(mapper::mapToDto).toList(), new PaginationCursor(params, histories.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public OrderHistoryResponseDto findById(UUID id) {
        var history = orderHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("base.error.not-found"));
        return mapper.mapToDto(history);
    }
}
