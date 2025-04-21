package com.alejandro.habitjourney.backend.user.controller;


import com.alejandro.habitjourney.backend.user.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UserControllerTestConfig {

    @Bean
    public UserController userController(UserService userService) {
        return new UserController(userService);
    }
}