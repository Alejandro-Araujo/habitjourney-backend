package com.alejandro.habitjourney.backend.auth.controller;

import com.alejandro.habitjourney.backend.auth.service.AuthService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AuthControllerTestConfig {
    @Bean
    public AuthController authController(AuthService authService) {
        return new AuthController(authService);
    }
}
