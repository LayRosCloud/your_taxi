package com.leafall.yourtaxi.utils;

import com.leafall.yourtaxi.dispatch.OrderAssignmentService;
import com.leafall.yourtaxi.dto.OfferAssignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.leafall.yourtaxi.dispatch.OrderAssignmentService.ACTIVE_OFFERS_KEY;
import static com.leafall.yourtaxi.middleware.LoggerMiddleware.HEADER_CORRELATION_LOG_ID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleUtils {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderAssignmentService assignmentService;
    private final SimpMessagingTemplate messagingTemplate;
    @Scheduled(fixedDelay = 1000)
    public void scheduleHandleOrder() {
        long now = System.currentTimeMillis();
        try {
            var correlationId = UUID.randomUUID();
            MDC.put(HEADER_CORRELATION_LOG_ID, correlationId.toString());
            log.debug(
                    "Вызывается scheduleHandleOrder: " + System.currentTimeMillis() / 1000);
            String luaScript =
                    "local key = KEYS[1] " +
                            "local now = tonumber(ARGV[1]) " +
                            "local expired = redis.call('ZRANGEBYSCORE', key, '-inf', now) " +
                            "if #expired > 0 then " +
                            "   redis.call('ZREMRANGEBYSCORE', key, '-inf', now) " +
                            "   return expired " +
                            "end " +
                            "return {}";
            DefaultRedisScript<List> script = new DefaultRedisScript<>(luaScript, List.class);

            var expiredOffers = stringRedisTemplate.execute(
                    script,
                    Collections.singletonList(ACTIVE_OFFERS_KEY),
                    String.valueOf(now)
            );
            if (expiredOffers == null || expiredOffers.isEmpty()) {
                log.debug("Не нашлось просроченных офферов");
                return;
            }
            log.info("Обнаружено {} истекших предложений {}", expiredOffers.size(), expiredOffers);
            for (Object offerJson : expiredOffers) {
                try {
                    var offer = objectMapper.readValue(offerJson.toString(), OfferAssignment.class);

                    var driverId = assignmentService.handleOfferTimeout(offer.getOrderId(), offer.getDriverId());
                    messagingTemplate.convertAndSendToUser(offer.getDriverId().toString(), "/queue/orders/cancel", offer);
                    if (driverId != null) {
                        messagingTemplate.convertAndSendToUser(driverId.toString(), "/queue/orders/new", offer);
                    }
                } catch (Exception e) {
                    log.error("Ошибка обработки истекшего предложения", e);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при schedule", e);
        } finally {
            MDC.clear();
        }
    }
}
