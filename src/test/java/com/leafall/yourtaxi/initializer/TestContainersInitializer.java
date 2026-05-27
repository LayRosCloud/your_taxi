package com.leafall.yourtaxi.initializer;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final int POSTGRES_PORT = 5432;
    private static final String POSTGRES_DB = "test";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "1111";

    private static final int REDIS_PORT = 6379;
    private static final String REDIS_PASSWORD = "admin";

    private static final GenericContainer<?> POSTGRES = new GenericContainer<>("postgis/postgis:15-3.4")
            .withEnv("POSTGRES_DB", POSTGRES_DB)
            .withEnv("POSTGRES_USER", POSTGRES_USER)
            .withEnv("POSTGRES_PASSWORD", POSTGRES_PASSWORD)
            .waitingFor(Wait.forListeningPort())
            .withExposedPorts(POSTGRES_PORT);

    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withCommand("redis-server", "--requirepass", REDIS_PASSWORD)
            .waitingFor(Wait.forListeningPort())
            .withExposedPorts(REDIS_PORT);

    static {
        POSTGRES.start();
        REDIS.start();
    }

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + jdbcUrl(),
                "spring.datasource.username=" + POSTGRES_USER,
                "spring.datasource.password=" + POSTGRES_PASSWORD,
                "REDIS_HOST=" + REDIS.getHost(),
                "REDIS_PORT=" + REDIS.getMappedPort(REDIS_PORT),
                "REDIS_PASS=" + REDIS_PASSWORD
        ).applyTo(applicationContext);
    }

    private static String jdbcUrl() {
        return "jdbc:postgresql://%s:%d/%s".formatted(
                POSTGRES.getHost(),
                POSTGRES.getMappedPort(POSTGRES_PORT),
                POSTGRES_DB
        );
    }
}
