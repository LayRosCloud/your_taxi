package com.leafall.yourtaxi.middleware;

import com.leafall.yourtaxi.dto.ErrorDto;
import com.leafall.yourtaxi.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/favicon.ico",
            "/actuator",
            "/swagger-ui/index.html"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String clientIp = getClientIp(httpRequest);

        boolean allowed = switch (getEndpointType(uri)) {
            case LOGIN -> rateLimitService.tryConsumeLogin(clientIp + ":login");
            case REGISTRATION -> rateLimitService.tryConsumeRegistration(clientIp + ":register");
            case API -> rateLimitService.tryConsumeApi(clientIp + ":api");
            default -> true;
        };

        if (allowed) {
            chain.doFilter(request, response);
        } else {
            sendRateLimitResponse(httpResponse, uri);
        }
    }

    private EndpointType getEndpointType(String uri) {
        if (EXCLUDED_PATHS.contains(uri)) {
            return EndpointType.OTHER;
        }
        if (uri.contains("/v1/users/signin")) {
            return EndpointType.LOGIN;
        } else if (uri.contains("/v1/users/signup")) {
            return EndpointType.REGISTRATION;
        } else if (uri.startsWith("/")) {
            log.info("Попал в API");
            return EndpointType.API;
        }
        return EndpointType.OTHER;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String path) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Retry-After", "900");
        response.setHeader("X-RateLimit-Limit", "5");
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 900));

        var errorDto = new ErrorDto(429, List.of("Too many request"));

        response.getWriter().write(objectMapper.writeValueAsString(errorDto));
    }

    enum EndpointType {
        LOGIN, REGISTRATION, API, OTHER
    }
}