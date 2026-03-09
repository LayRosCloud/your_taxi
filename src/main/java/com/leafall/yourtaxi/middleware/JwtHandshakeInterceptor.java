package com.leafall.yourtaxi.middleware;

import com.leafall.yourtaxi.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = extractTokenFromHeader(servletRequest);

            if (token == null || token.isEmpty()) {
                token = extractTokenFromQuery(servletRequest);
            }
            if (token != null) {
                tokenService.validateAccessToken(token);
                var claims = tokenService.getAccessClaims(token);
                log.info("Токен провалидирован и достал id {} юзера", claims.getSubject());
                attributes.put("DRIVER_ID", claims.getSubject());
                attributes.put("user", claims.getSubject());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {
    }

    private String extractTokenFromHeader(ServletServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String extractTokenFromQuery(ServletServerHttpRequest request) {
        return request.getServletRequest().getParameter("token");
    }
}
