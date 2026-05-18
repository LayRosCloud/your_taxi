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
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }
    }

}
