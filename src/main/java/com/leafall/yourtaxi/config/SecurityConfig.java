package com.leafall.yourtaxi.config;

import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.middleware.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_LIST_URLS = {"/v1/users/signin", "/v1/users/signup","/v1/users/verify", "/v1/users/tokens/refresh", "/v3/api-docs/**","/swagger-ui/**","/swagger-ui"};

    private static final String[] USER_LIST_URLS = {"/v1/client/**"};
    private static final String[] WORKER_LIST_URLS = {"/v1/app/**"};
    private static final String[] DISPATCHER_LIST_URLS = {"/v1/admin/**"};
    private final AuthenticationProvider provider;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_LIST_URLS).permitAll()
                        .requestMatchers(USER_LIST_URLS).hasAuthority(UserRole.USER.name())
                        .requestMatchers(WORKER_LIST_URLS).hasAuthority(UserRole.EMPLOYEE.name())
                        .requestMatchers(DISPATCHER_LIST_URLS).hasAuthority(UserRole.DISPATCHER.name())
                        .anyRequest().authenticated()
                )
                .authenticationProvider(provider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
