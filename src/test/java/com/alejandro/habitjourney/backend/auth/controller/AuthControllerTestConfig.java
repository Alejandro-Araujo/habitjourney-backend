package com.alejandro.habitjourney.backend.auth.controller;

import com.alejandro.habitjourney.backend.auth.service.AuthService;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AuthControllerTestConfig {
    @Bean
    public AuthController authController(AuthService authService, UserMapper userMapper) {
        return new AuthController(authService, userMapper);
    }
}
