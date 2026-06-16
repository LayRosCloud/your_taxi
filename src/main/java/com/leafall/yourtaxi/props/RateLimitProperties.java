package com.leafall.yourtaxi.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class RateLimitProperties {
    private EndpointConfig login = new EndpointConfig();
    private EndpointConfig registration = new EndpointConfig();
    private EndpointConfig api = new EndpointConfig();

    @Data
    public static class EndpointConfig {
        private int maxAttempts = 100;
        private int windowMinutes = 1;
        private int blockDurationMinutes = 30;
    }
}
