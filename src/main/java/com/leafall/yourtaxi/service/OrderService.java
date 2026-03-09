package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.order.OrderCostDto;
import com.leafall.yourtaxi.dto.order.OrderCreateDto;
import com.leafall.yourtaxi.dto.order.OrderRedisWaitingDto;
import com.leafall.yourtaxi.dto.order.OrderResponseDto;
import com.leafall.yourtaxi.dto.point.PointCostDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.PointEntity;
import com.leafall.yourtaxi.entity.TripEntity;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.exception.ConflictException;
import com.leafall.yourtaxi.exception.ForbiddenException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.OrderMapper;
import com.leafall.yourtaxi.mapper.PointMapper;
import com.leafall.yourtaxi.repository.*;
import com.leafall.yourtaxi.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.leafall.yourtaxi.config.ConstantsConfig.BIG_ORDER_FROM_KEY;
import static com.leafall.yourtaxi.config.ConstantsConfig.PRICE_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final GeoService geoService;
    private final PointMapper pointMapper;
    private final OrderMapper orderMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TripRepository tripRepository;
    private final VariableRepository variableRepository;
    private static final String ORDERS_KEY = "orders:employees:";

    @Transactional(readOnly = true)
    public OrderResponseDto findActiveOrder() {
        var currentUserId = SecurityUtils.getCurrentUserId();
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var statuses = new ArrayList<OrderStatus>();
        statuses.add(OrderStatus.COMPLETED);
        statuses.add(OrderStatus.REJECTED);
        OrderEntity order = null;
        if (user.getRole() == UserRole.USER) {
            var orders = orderRepository.findAllByUserAndStatusNotIn(user, statuses);
            if (orders.size() == 0) {
                throw new NotFoundException("order.error.not-found");
            }
            order = orders.get(0);
        } else {
            var trip = validateActualTrip();
            var orders = orderRepository.findAllByExecutorAndStatusNotIn(trip, statuses);
            if (orders.size() == 0) {
                throw new NotFoundException("order.error.not-found");
            }
            order = orders.get(0);
        }
        return orderMapper.mapToDto(order);
    }

    public PointCostDto getCostAndDuration(OrderCostDto dto) {
        var distances = geoService.getDistance(dto.getFrom(), dto.getTo());
        if (distances.getRoutes().size() == 0) {
            log.error("Пришло 0 роутов! Невозможно посчитать стоимость!");
            throw new BadRequestException();
        }
        var distance = distances.getRoutes().get(0);
        var point = new PointCostDto();
        point.setDurationInSeconds(distance.getDuration());
        var list = new ArrayList<String>(2);
        list.add(PRICE_KEY);
        list.add(BIG_ORDER_FROM_KEY);

        var keys = variableRepository.findAllByKeyIn(list);
        if (keys.size() != list.size()) {
            throw new NotFoundException("key.error.not-found");
        }
        var map = new HashMap<String, String>();
        for (var key: keys) {
            map.put(key.getKey(), key.getValue());
        }
        var distanceKilometers = distance.getDistance() / 1000;
        var price = Double.parseDouble(map.get(PRICE_KEY));
        var distanceLimit = Double.parseDouble(map.get(BIG_ORDER_FROM_KEY));
        point.setPrice(distanceKilometers * price);
        point.setIsBigDistance(distanceKilometers < distanceLimit);
        return point;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto create(OrderCreateDto dto) {
        var currentUserId = SecurityUtils.getCurrentUserId();
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var statuses = new ArrayList<OrderStatus>();
        statuses.add(OrderStatus.COMPLETED);
        statuses.add(OrderStatus.REJECTED);
        var existsOrder = orderRepository.findAllByUserAndStatusNotIn(user, statuses);
        if (existsOrder.size() > 0) {
            throw new ConflictException("order.error.exists-order");
        }
        var geos = geoService.getNearbyDrivers(dto.getFrom().getLongitude(), dto.getFrom().getLatitude(), dto.getRadius());
        if (geos.size() == 0) {
            throw new BadRequestException("order.error.not-valid");
        }
        var costAndDuration = getCostAndDuration(dto);

        var toSave = new OrderEntity();
        toSave.setPrice(costAndDuration.getPrice());
        toSave.setUser(user);
        toSave.setStatus(OrderStatus.NEW);
        var newOrder = orderRepository.save(toSave);

        var pointsToSave = new ArrayList<PointEntity>(2);
        pointsToSave.add(pointMapper.mapToEntity(dto.getFrom(), newOrder, 0));
        pointsToSave.add(pointMapper.mapToEntity(dto.getTo(), newOrder, 1));
        var points = pointRepository.saveAll(pointsToSave);

        newOrder.setPoints(points);
        var firstId = geos.getFirst().getId().toString();
        var setIds = new HashSet<String>(1);
        setIds.add(firstId);

        var order = new OrderRedisWaitingDto();
        order.setId(newOrder.getId());
        order.setLatitude(dto.getFrom().getLatitude());
        order.setLongitude(dto.getFrom().getLongitude());
        order.setRadius(dto.getRadius());
        order.setIds(setIds);
        System.out.println(order);
        var orderDto = orderMapper.mapToDto(newOrder);
        System.out.println(firstId);
        redisTemplate.opsForValue().set(String.format("%s%s", ORDERS_KEY, newOrder.getId().toString()), order, 30, TimeUnit.MINUTES);
        messagingTemplate.convertAndSendToUser(firstId, "/queue/orders/new", orderDto);
        return orderDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto accept(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        order.setStatus(OrderStatus.ACCEPT);
        order.setExecutor(trip);
        var newOrder = orderRepository.save(order);
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto cancel(UUID id) {
        var order = (OrderRedisWaitingDto)redisTemplate.opsForValue().get(String.format("%s%s", ORDERS_KEY, id));
        if (order == null) {
            throw new NotFoundException("order.error.not-found");
        }
        var geos = geoService.getNearbyDrivers(order.getLongitude(), order.getLatitude(), order.getRadius());
        geos = geos.stream().filter(x -> !order.getIds().contains(x.getId().toString())).toList();
        var findedOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        var orderDto = orderMapper.mapToDto(findedOrder);

        if (geos.size() > 0) {
            var executorId = geos.getFirst().getId().toString();
            order.getIds().add(executorId);
            redisTemplate.opsForValue().set(String.format("%s%s", ORDERS_KEY, order.getId().toString()), order, 30, TimeUnit.MINUTES);

            if (findedOrder.getStatus() != OrderStatus.NEW) {
                throw new ConflictException("order.error.not-valid");
            }
            messagingTemplate.convertAndSendToUser(executorId, "/queue/orders/new", orderDto);
        } else {
            findedOrder.setStatus(OrderStatus.REJECTED);
            var updatedOrder = orderRepository.save(findedOrder);
            orderDto = orderMapper.mapToDto(updatedOrder);
            messagingTemplate.convertAndSendToUser(findedOrder.getUser().getId().toString(), "/topic/orders/reject", orderDto);
        }
        return orderDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto expectOrder(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (!trip.getId().equals(order.getExecutor().getId())) {
            throw new ForbiddenException("order.error.not-executor");
        }
        if (order.getStatus() != OrderStatus.ACCEPT) {
            throw new ConflictException("order.error.bad-status");
        }
        order.setStatus(OrderStatus.EXPECTATION);
        var newOrder = orderRepository.save(order);
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto processOrder(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (!trip.getId().equals(order.getExecutor().getId())) {
            throw new ForbiddenException("order.error.not-executor");
        }
        if (order.getStatus() != OrderStatus.EXPECTATION) {
            throw new ConflictException("order.error.bad-status");
        }
        order.setStatus(OrderStatus.IN_PROCESS);
        var newOrder = orderRepository.save(order);
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto completeOrder(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (!trip.getId().equals(order.getExecutor().getId())) {
            throw new ForbiddenException("order.error.not-executor");
        }
        order.setStatus(OrderStatus.COMPLETED);
        var newOrder = orderRepository.save(order);
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto reject(UUID id) {
        var currentUserId = SecurityUtils.getCurrentUserId();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (order.getUser().getId() != currentUserId) {
            throw new ForbiddenException("order.error.not-found");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ConflictException("order.error.bad-status");
        }
        order.setStatus(OrderStatus.REJECTED);
        var newOrder = orderRepository.save(order);
        return orderMapper.mapToDto(newOrder);
    }

    private TripEntity validateActualTrip() {
        var userId = SecurityUtils.getCurrentUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        return tripRepository.findByUserAndEndAtIsNull(user)
                .orElseThrow(() -> new NotFoundException("trip.error.not-found"));
    }
}
