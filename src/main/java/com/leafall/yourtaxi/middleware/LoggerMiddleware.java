package com.leafall.yourtaxi.middleware;

import com.leafall.yourtaxi.utils.TimeUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.LogRecord;

@Component
@Slf4j
public class LoggerMiddleware implements Filter {
    public static final String HEADER_CORRELATION_ID  = "X-Correlation-Id";
    public static final String HEADER_CORRELATION_LOG_ID  = "correlationId";
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;
        String correlationId = httpRequest.getHeader(HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        response.setHeader(HEADER_CORRELATION_ID, correlationId);
        MDC.put(HEADER_CORRELATION_LOG_ID, correlationId);
        var currentTime = TimeUtils.getCurrentTimeFromUTC();
        try {
            logStart(httpRequest, correlationId);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception error) {
            logError(httpRequest, correlationId, error);
            throw error;
        } finally {
            var newCurrentTime = TimeUtils.getCurrentTimeFromUTC();
            logEnd(httpRequest, correlationId, newCurrentTime - currentTime);

            MDC.clear();
        }
    }

    public void logStart(HttpServletRequest request, String correlationId) {
        log.info("[START][{}] {} {}", correlationId, request.getMethod(), request.getRequestURI());
    }

    public void logEnd(HttpServletRequest request, String correlationId, Long ticks) {
        log.info("[END][{}] {} {} {} ms", correlationId, request.getMethod(), request.getRequestURI(), ticks);
    }

    public void logError(HttpServletRequest request, String correlationId, Exception error) {
        log.error("[ERROR][{}] {} {}: {}", correlationId, request.getMethod(), request.getRequestURI(), error.getMessage());
    }
}
