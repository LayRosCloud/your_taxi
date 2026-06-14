package com.leafall.yourtaxi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;

@OpenAPIDefinition(
        info =@Info(
                title = "Your Taxi API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Leafall", email = "vogistv@gmail.com", url = "https://protobin.com/"
                ),
                license = @License(
                        name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = @Server(
                url = "/",
                description = "All servers"
        )
)
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer globalHeadersCustomizer() {
        return openApi -> {
            var acceptLanguageHeader = new HeaderParameter()
                    .in("header")
                    .name("Accept-Language")
                    .description("Язык ответа (например: ru, en)")
                    .required(false)
                    .schema(new StringSchema()._default("ru").addEnumItem("ru").addEnumItem("en"));

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {

                        operation.addParametersItem(acceptLanguageHeader);

                        operation.getResponses().values().forEach(response -> {
                            var headers = response.getHeaders();
                            if (headers == null) {
                                headers = new LinkedHashMap<>();
                            }
                            headers.put("X-Correlation-Id", new Header()
                                    .description("Идентификационный номер запроса")
                                    .schema(new StringSchema().example("f46e06c0-6c3f-481b-a28a-6aec31095918"))
                            );
                            headers.put("Content-Type", new Header()
                                    .description("Тип возвращаемого контента")
                                    .schema(new StringSchema().example("application/json;charset=UTF-8"))
                            );
                            response.setHeaders(headers);
                        });
                    })
            );
        };
    }
}
