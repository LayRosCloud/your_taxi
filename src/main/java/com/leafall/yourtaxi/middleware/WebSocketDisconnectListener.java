package com.leafall.yourtaxi.middleware;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketDisconnectListener {

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String id = (String) headerAccessor.getSessionAttributes().get("user");

        log.info("Пользователь id={} отключился, sessionId={}", id, sessionId);

    }
}