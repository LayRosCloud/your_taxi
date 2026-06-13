package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dispatch.DriverDispatchService;
import com.leafall.yourtaxi.dispatch.GeoService;
import com.leafall.yourtaxi.dispatch.OrderAssignmentService;
import com.leafall.yourtaxi.dispatch.SearchService;
import com.leafall.yourtaxi.dto.order.*;
import com.leafall.yourtaxi.dto.point.PointCostDto;
import com.leafall.yourtaxi.dto.point.PointCreateDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.OrderHistoryEntity;
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
import com.leafall.yourtaxi.repository.specification.OrderSpecification;
import com.leafall.yourtaxi.utils.TimeUtils;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import com.leafall.yourtaxi.utils.request.OrderQueryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;

import static com.leafall.yourtaxi.config.ConstantsConfig.BIG_ORDER_FROM_KEY;
import static com.leafall.yourtaxi.config.ConstantsConfig.PRICE_KEY;
import static com.leafall.yourtaxi.dispatch.OrderAssignmentService.MAX_RADIUS_SEARCH;
import static com.leafall.yourtaxi.utils.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final GeoService geoService;
    private final DriverDispatchService dispatchService;
    private final PointMapper pointMapper;
    private final OrderMapper orderMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final TripRepository tripRepository;
    private final VariableRepository variableRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final SearchService searchService;
    private final OrderAssignmentService orderAssignmentService;

    private static final List<OrderStatus> NOT_FINISHED_STATUSES = List.of(OrderStatus.COMPLETED, OrderStatus.REJECTED);
    @Transactional(readOnly = true)
    public OrderResponseWithDurationDto findActiveOrder() {
        var currentUserId = getCurrentUserId();
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        OrderEntity order = null;
        if (user.getRole() == UserRole.USER) {
            var orders = orderRepository.findAllByUserAndStatusNotIn(user, NOT_FINISHED_STATUSES);
            if (orders.size() == 0) {
                log.info("Заказов не было найдено для пользователя {}", currentUserId);
                throw new NotFoundException("order.error.not-found");
            }
            order = orders.get(0);
        } else {
            var trip = validateActualTrip();
            var orders = orderRepository.findAllByExecutorAndStatusNotIn(trip, NOT_FINISHED_STATUSES);
            if (orders.size() == 0) {
                log.info("Заказов не было найдено для исполнителя {}", currentUserId);
                throw new NotFoundException("order.error.not-found");
            }
            order = orders.get(0);
        }
        var mappedOrder = orderMapper.mapToDtoDuration(order);
        if (order.getExecutor() != null) {

            var distance = geoService.getDriverLocation(order.getExecutor().getUser().getId());
            if (distance.isPresent()) {
                var pointOfUsers = order.getPoints().stream().filter(item -> item.getIndex() == 0).toList();
                if (pointOfUsers.size() == 0) {
                    log.info("Неправильно состояние заказа у пользователя {}", currentUserId);
                    throw new NotFoundException("order.error.not-found");
                }
                var pointOfUser = pointOfUsers.get(0);
                var dto = new PointCreateDto();
                dto.setLongitude(pointOfUser.getPoint().getCoordinate().x);
                dto.setLatitude(pointOfUser.getPoint().getCoordinate().y);

                var dto1 = new PointCreateDto();
                dto1.setLongitude(distance.get().getLongitude());
                dto1.setLatitude(distance.get().getLatitude());
                var distances = geoService.getDistance(dto, dto1);
                if (distances.getRoutes().size() == 0) {
                    log.error("Пришло 0 роутов! Невозможно посчитать стоимость!");
                    throw new BadRequestException();
                }
                var route = distances.getRoutes().get(0);
                mappedOrder.setDurationInSeconds(route.getDuration());
            }

        }
        return mappedOrder;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<OrderResponseDto> findAll(OrderQueryDto query) {
        var specification = OrderSpecification.hasStatus(query.getStatus())
                .and(OrderSpecification.betweenPrice(query.getPriceFrom(), query.getPriceTo()))
                .and(OrderSpecification.betweenDate(query.getDateFrom() != null ? Date.valueOf(query.getDateFrom()) : null, query.getDateTo() != null ?  Date.valueOf(query.getDateTo()) : null))
                .and(OrderSpecification.hasClientId(query.getUserId()))
                .and(OrderSpecification.hasDriver(query.getExecutorId()))
                .and(OrderSpecification.hasIsBigDistance(query.getIsBigDistance()))
                .and(OrderSpecification.hasPaymentType(query.getPaymentType()));
        var params = new PaginationParams(query.getLimit(), query.getPage());
        var pageable = params.getPageable(query.getIsAscending(), "createdAt");
        var orders = orderRepository.findAll(specification, pageable);
        var result = orders.stream().map(orderMapper::mapToDto).toList();
        return new PaginationResponse<>(result, new PaginationCursor(params, orders.getTotalElements()));
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
        point.setIsBigDistance(distanceKilometers >= distanceLimit);
        return point;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto create(OrderCreateDto dto) {
        var currentUserId = getCurrentUserId();
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var existsOrder = orderRepository.findAllByUserAndStatusNotIn(user, NOT_FINISHED_STATUSES);
        if (existsOrder.size() > 0) {
            log.warn("У пользователя {} есть 1 или более активных заказов. Нельзя создать новый", currentUserId);
            throw new ConflictException("order.error.exists-order");
        }
        var costAndDuration = getCostAndDuration(dto);

        var toSave = orderMapper.mapToEntity(costAndDuration, dto.getPaymentType(), user, OrderStatus.NEW);
        var newOrder = orderRepository.save(toSave);
        log.debug("Заказ создан id={}, currentUserId={}", newOrder.getId(), currentUserId);
        var pointsToSave = new ArrayList<PointEntity>(2);
        pointsToSave.add(pointMapper.mapToEntity(dto.getFrom(), newOrder, 0));
        pointsToSave.add(pointMapper.mapToEntity(dto.getTo(), newOrder, 1));
        var points = pointRepository.saveAll(pointsToSave);
        log.debug("Точки созданы для заказа {}", newOrder.getId());
        newOrder.setPoints(points);
        var orderDto = orderMapper.mapToDto(newOrder);
        if (toSave.getIsBigDistance()) {
            var dispatcher = userRepository.findByRole(UserRole.DISPATCHER)
                    .orElseThrow(() -> new NotFoundException("user.error.not-found"));
            log.info("Заказ будет отправлен диспетчеру {}", dispatcher.getId());
            messagingTemplate.convertAndSendToUser(dispatcher.getId().toString(), "/queue/orders/new", orderDto);
            createOrderHistory(newOrder, String.format("Заказ создан пользователем \"%s\" и отправлен диспетчеру \"%s\" на рассмотрение", user.getFullName(), dispatcher.getFullName()), null);
        } else {

            var driverId = searchService.findDriverForOrder(dto.getFrom().getLongitude(), dto.getFrom().getLatitude(), MAX_RADIUS_SEARCH, newOrder.getId());
            if (driverId == null) {
                log.warn("В системе никого нет");
                throw new BadRequestException("order.error.not-found-executor");
            }
            var order = new OrderRedisWaitingDto();
            order.setId(newOrder.getId());
            order.setLatitude(dto.getFrom().getLatitude());
            order.setLongitude(dto.getFrom().getLongitude());
            order.setIds(Set.of(driverId.toString()));
            log.info("Заказ будет отправлен исполнителю {}", driverId);
            searchService.addToOrderQueue(order);
            messagingTemplate.convertAndSendToUser(driverId.toString(), "/queue/orders/new", orderDto);
            createOrderHistory(newOrder, String.format("Заказ создан пользователем \"%s\" и отправлен исполнителю \"%s\" на принятие",
                            user.getFullName(), driverId), null);
        }

        return orderDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto accept(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Заказ {} не найден", id);
                    return new NotFoundException("order.error.not-found");
                });
        if (order.getStatus() != OrderStatus.NEW || order.getIsBigDistance()) {
            log.info("Заказ {} уже имеет не новый статус. Невозможно принять или большую дистанцию", id);
            throw new ConflictException("order.error.not-valid");
        }
        order.setStatus(OrderStatus.ACCEPT);
        order.setExecutor(trip);
        var geo = geoService.getDriverLocation(trip.getUser().getId()).orElse(null);

        createOrderHistory(order, String.format("[Система подбора] Заказ принят исполнителем \"%s\"",
                        trip.getUser().getFullName()), geoService.mapFromDtoToPoint(geo));
        dispatchService.removeFromQueue(getCurrentUserId());
        var newOrder = orderRepository.save(order);
        orderAssignmentService.removeActiveOffer(id, order.getId());
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto cancel(UUID id) {
        var order = searchService.getOrderFromRedis(id);
        if (order == null) {
            log.warn("Заказ {} из Redis не найден", id);
            throw new NotFoundException("order.error.not-found");
        }
        var findedOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (findedOrder.getStatus() != OrderStatus.NEW) {
            log.info("Заказ {} уже имеет статус {}. Невозможно найти для него исполнителя", findedOrder.getId(), findedOrder.getStatus());
            throw new ConflictException("order.error.not-valid");
        }
        orderAssignmentService.removeActiveOffer(id, findedOrder.getId());
        var driverForOrder = searchService.findDriverForOrder(order.getLongitude(), order.getLatitude(), MAX_RADIUS_SEARCH, id);

        var currentUser = geoService.getDriverLocation(getCurrentUserId()).orElse(null);

        dispatchService.addToQueue(getCurrentUserId());

        createOrderHistory(findedOrder, String.format("[Система подбора] Заказ отклонен исполнителем \"%s\". Начинаю искать нового",
                getCurrentUserId()), geoService.mapFromDtoToPoint(currentUser));

        var orderDto = orderMapper.mapToDto(findedOrder);

        if (driverForOrder != null) {
            order.getIds().add(driverForOrder.toString());
            searchService.addToOrderQueue(order);
            log.info("Заказу {} будет отправлен новый исполнитель {}", findedOrder.getId(), driverForOrder);
            messagingTemplate.convertAndSendToUser(driverForOrder.toString(), "/queue/orders/new", orderDto);
            createOrderHistory(findedOrder, String.format("[Система подбора] Найден новый исполнитель %s. Начинаю отправку",
                    driverForOrder), geoService.mapFromDtoToPoint(currentUser));
        } else {
            findedOrder.setStatus(OrderStatus.REJECTED);
            var updatedOrder = orderRepository.save(findedOrder);
            orderDto = orderMapper.mapToDto(updatedOrder);
            messagingTemplate.convertAndSendToUser(findedOrder.getUser().getId().toString(), "/topic/orders/reject", orderDto);
            createOrderHistory(findedOrder, "[Система подбора] Исполнителей не было найдено для заказа. Произвожу отмену", geoService.mapFromDtoToPoint(currentUser));
        }
        return orderDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto expectOrder(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (!trip.getId().equals(order.getExecutor().getId())) {
            log.info("Исполнителем заказа {} является исполнитель {}, не {}. Он не может принять в ожидание.", id, order.getExecutor().getId(), trip.getId());
            throw new ForbiddenException("order.error.not-executor");
        }
        if (order.getStatus() != OrderStatus.ACCEPT) {
            log.info("Заказ {} уже имеет статус {}. Невозможно поставить в ожидание", id, order.getStatus());
            throw new ConflictException("order.error.bad-status");
        }
        var currentUser = geoService.getDriverLocation(getCurrentUserId()).orElse(null);
        order.setStatus(OrderStatus.EXPECTATION);
        order.setScheduledStartTime(TimeUtils.getCurrentTimeFromUTC());
        var newOrder = orderRepository.save(order);
        createOrderHistory(newOrder, "[Действия над заказом] Исполнитель поставил заказ в ожидание", geoService.mapFromDtoToPoint(currentUser));
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto processOrder(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (!trip.getId().equals(order.getExecutor().getId())) {
            log.info("Исполнителем заказа {} является исполнитель {}, не {}. Он не может начать заказ.", id, order.getExecutor().getId(), trip.getId());
            throw new ForbiddenException("order.error.not-executor");
        }
        if (order.getStatus() != OrderStatus.EXPECTATION) {
            log.info("Заказ {} уже имеет статус {}. Невозможно начать в работу", id, order.getStatus());
            throw new ConflictException("order.error.bad-status");
        }
        var currentUser = geoService.getDriverLocation(getCurrentUserId()).orElse(null);
        order.setStatus(OrderStatus.IN_PROCESS);
        var newOrder = orderRepository.save(order);
        createOrderHistory(newOrder, "[Действия над заказом] Исполнитель начал выполнение заказа", geoService.mapFromDtoToPoint(currentUser));
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto completeOrder(UUID id) {
        var trip = validateActualTrip();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (!trip.getId().equals(order.getExecutor().getId())) {
            log.info("Исполнителем заказа {} является исполнитель {}, не {}. Он не может завершить.", id, order.getExecutor().getId(), trip.getId());
            throw new ForbiddenException("order.error.not-executor");
        }
        if (order.getStatus() != OrderStatus.IN_PROCESS) {
            log.info("Заказ {} уже имеет статус {}. Невозможно завершить", id, order.getStatus());
            throw new ConflictException("order.error.not-valid");
        }
        order.setStatus(OrderStatus.COMPLETED);
        var newOrder = orderRepository.save(order);
        var currentUser = geoService.getDriverLocation(getCurrentUserId()).orElse(null);
        dispatchService.addToQueue(order.getExecutor().getUser().getId());
        createOrderHistory(newOrder, "[Действия над заказом] Исполнитель завершил выполнение заказа", geoService.mapFromDtoToPoint(currentUser));
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto setDateAndDriver(OrderSetDriverAndDate dto) {
        var order = orderRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        var driver = userRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        if (!order.getIsBigDistance()) {
            throw new ForbiddenException();
        }
        order.setScheduledStartTime(dto.getDate().getTime());
        order.setPlannerDriver(driver);
        order.setStatus(OrderStatus.ACCEPT);
        var newOrder = orderRepository.save(order);
        createOrderHistory(newOrder, String.format("[Действия над заказом] Диспетчер \"%s\" установил исполнителя \"%s\" для заказа", getCurrentUserId(), driver.getFullName()), null);
        return orderMapper.mapToDto(newOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto reject(UUID id) {
        var currentUserId = getCurrentUserId();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.error.not-found"));
        if (!order.getUser().getId().equals(currentUserId)) {
            log.info("Заказчиком заказа {} является пользователь {}, не {}. Он не может отклонить.", id, order.getUser().getId(), currentUserId);
            throw new ForbiddenException("order.error.not-found");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            log.info("Заказ {} уже имеет статус {}. Невозможно отклонить", id, order.getStatus());
            throw new ConflictException("order.error.bad-status");
        }
        order.setStatus(OrderStatus.REJECTED);
        var newOrder = orderRepository.save(order);
        createOrderHistory(newOrder, "[Действия над заказом] Заказчик отменил выполнение заказа", null);
        return orderMapper.mapToDto(newOrder);
    }

    private TripEntity validateActualTrip() {
        var userId = getCurrentUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        return tripRepository.findByUserAndEndAtIsNull(user)
                .orElseThrow(() -> new NotFoundException("trip.error.not-found"));
    }

    private void createOrderHistory(OrderEntity order, String message, Geometry point) {
        var orderHistory = new OrderHistoryEntity();
        orderHistory.setOrder(order);
        orderHistory.setStatus(order.getStatus());
        orderHistory.setMessage(message);
        orderHistory.setPoint(point);
        orderHistoryRepository.save(orderHistory);
    }
}
