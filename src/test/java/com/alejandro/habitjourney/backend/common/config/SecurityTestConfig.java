package com.alejandro.habitjourney.backend.common.config;

import com.alejandro.habitjourney.backend.common.exception.GlobalExceptionHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@TestConfiguration
@Import(GlobalExceptionHandler.class)
public class SecurityTestConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permite todas las rutas para simplificar tests
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {})
                        .accessDeniedHandler((request, response, accessDeniedException) -> {})
                );
        return http.build();
    }

}