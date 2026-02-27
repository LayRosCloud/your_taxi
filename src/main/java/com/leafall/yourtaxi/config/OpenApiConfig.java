package com.leafall.yourtaxi.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class OpenApiConfig {
    @Value("${server.host}")
    private String serverHost;

    @Bean
    public OpenAPI openApi() {
        var server = new Server()
                .url("/")
                .variables(
                        new ServerVariables()
                                .addServerVariable("host", new ServerVariable()
                                        ._default(serverHost)
                                )
                );
        var security = new SecurityRequirement().addList("Authorization");
        return new OpenAPI()
                .addSecurityItem(security)
                .addServersItem(server);
    }
}
