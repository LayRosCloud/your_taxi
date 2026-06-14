package com.leafall.yourtaxi.utils;

import com.leafall.yourtaxi.dispatch.OrderAssignmentService;
import com.leafall.yourtaxi.dto.OfferAssignment;
import com.leafall.yourtaxi.dto.order.history.OrderHistoryCreateDto;
import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.OrderHistoryEntity;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import com.leafall.yourtaxi.repository.OrderHistoryRepository;
import com.leafall.yourtaxi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.leafall.yourtaxi.dispatch.OrderAssignmentService.ACTIVE_OFFERS_KEY;
import static com.leafall.yourtaxi.middleware.LoggerMiddleware.HEADER_CORRELATION_LOG_ID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleUtils {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderAssignmentService assignmentService;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderRepository orderRepository;

    @Scheduled(fixedDelay = 1000)
    public void scheduleHandleOrder() {
        long now = System.currentTimeMillis();
        try {
            var correlationId = UUID.randomUUID();
            MDC.put(HEADER_CORRELATION_LOG_ID, correlationId.toString());
            log.debug(
                    "Вызывается scheduleHandleOrder: " + System.currentTimeMillis() / 1000);
            var expiredOffers = redisTemplate.opsForZSet()
                    .rangeByScore(ACTIVE_OFFERS_KEY, 0, now);

            if (expiredOffers == null || expiredOffers.isEmpty()) {
                log.debug("Не нашлось просроченных офферов");
                return;
            }

            redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_OFFERS_KEY, 0, now);

            log.info("Обнаружено {} истекших предложений", expiredOffers.size());
            var list = new ArrayList<OrderHistoryCreateDto>();
            for (Object offerJson : expiredOffers) {
                try {
                    log.debug("Обрабатываем JSON: {}", offerJson);

                    OfferAssignment offer = objectMapper.readValue(offerJson.toString(), OfferAssignment.class);

                    var driverId = assignmentService.handleOfferTimeout(offer.getOrderId(), offer.getDriverId());
                    var history2 = new OrderHistoryCreateDto();
                    history2.setOrderId(offer.getOrderId());
                    history2.setMessage("[Система подбора] Сотрудник не принял заказ в течении времени. Ищем нового.");
                    list.add(history2);
                    log.info("Заказ {} отклонен для пользователя {}", offer.getOrderId(), offer.getDriverId());
                    messagingTemplate.convertAndSendToUser(offer.getDriverId().toString(), "/queue/orders/cancel", offer);
                    var history = new OrderHistoryCreateDto();
                    history.setOrderId(offer.getOrderId());
                    if (driverId != null) {
                        log.info("Заказ {} отправлен для пользователя {}", offer.getOrderId(), driverId);
                        messagingTemplate.convertAndSendToUser(driverId.toString(), "/queue/orders/new", offer);
                        history.setMessage("[Система подбора] Нашелся сотрудник, автоматически отправляем приглашение");
                    } else {
                        log.info("Для заказ {} водитель второй не нашелся, отменяю заказ", offer.getOrderId());
                        history.setMessage("[Система подбора] Сотрудник не нашелся, отменяю заказ");
                        history.setOrderStatus(OrderStatus.REJECTED);
                    }
                    list.add(history);

                } catch (Exception e) {
                    log.error("Ошибка обработки истекшего предложения", e);
                }
            }
            var histories = new ArrayList<OrderHistoryEntity>();
            log.debug("Ищу по id {} заказов", list.size());
            var orders = orderRepository.findAllByIdIn(list.stream().map(OrderHistoryCreateDto::getOrderId).toList());
            log.debug("Нашел {} заказов", orders.size());
            var map = new HashMap<UUID, OrderEntity>();
            for (var order: orders) {
                map.put(order.getId(), order);
            }
            for (var history: list) {
                var toSave = new OrderHistoryEntity();
                if (!map.containsKey(history.getOrderId())) {
                    log.warn("Нет {} заказа. Пропускаю", history.getOrderId());
                    continue;
                }
                var order = map.get(history.getOrderId());
                toSave.setOrder(order);
                if (history.getOrderStatus() != null) {
                    toSave.setStatus(history.getOrderStatus());
                    order.setStatus(history.getOrderStatus());
                    orderRepository.save(order);
                } else {
                    toSave.setStatus(order.getStatus());
                }
                toSave.setMessage(history.getMessage());
                histories.add(toSave);
            }
            orderHistoryRepository.saveAll(histories);
        } catch (Exception e) {
            log.error("Ошибка при schedule", e);
        } finally {
            MDC.clear();
        }
    }
}
