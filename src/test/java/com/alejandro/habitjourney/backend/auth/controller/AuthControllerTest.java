package com.alejandro.habitjourney.backend.auth.controller;

import com.alejandro.habitjourney.backend.auth.dto.*;
import com.alejandro.habitjourney.backend.auth.service.AuthService;
import com.alejandro.habitjourney.backend.auth.service.AuthService.LoginResult;
import com.alejandro.habitjourney.backend.common.config.SecurityTestConfig;
import com.alejandro.habitjourney.backend.common.exception.TestExceptionConfig;
import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.constant.SuccessMessages;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.config.TestConfig;
import com.alejandro.habitjourney.backend.common.security.JwtAuthenticationFilter;
import com.alejandro.habitjourney.backend.common.util.TestDataFactory;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de slice (@WebMvcTest) para la clase de controlador {@link AuthController}.
 * Se enfoca en verificar el manejo de las solicitudes HTTP,
 * las respuestas y la correcta integración con el servicio {@link AuthService} (mockeado)
 * para los endpoints de autenticación (registro y login).
 * Configura un contexto mínimo de Spring MVC para las pruebas.
 */
@ContextConfiguration(classes = {
        SecurityTestConfig.class,
        AuthControllerTestConfig.class,
        TestExceptionConfig.class
})
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest extends TestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private RegisterRequestDTO validRegisterRequest;
    private LoginRequestDTO validLoginRequest;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        // Arrange
        validRegisterRequest = TestDataFactory.createValidRegisterRequest();
        validLoginRequest = TestDataFactory.createValidLoginRequest();
        testUser = TestDataFactory.createTestUser();
        testUserDTO = TestDataFactory.createTestUserDTO();
    }

    @Test
    void givenValidRegisterRequest_whenRegister_thenReturnsCreated() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(testUser);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_REGISTERED_SUCCESS))
                .andExpect(jsonPath("$.user.id").value(testUserDTO.getId()))
                .andExpect(jsonPath("$.user.name").value(testUserDTO.getName()))
                .andExpect(jsonPath("$.user.email").value(testUserDTO.getEmail()));
    }

    @Test
    void givenExistingEmail_whenRegister_thenReturnsBadRequest() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException(ErrorMessages.EMAIL_EXISTS));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_CONFLICT))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.EMAIL_EXISTS))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenInvalidRegisterRequest_whenRegister_thenReturnsValidationErrors() throws Exception {
        // Arrange
        RegisterRequestDTO invalidRequest = TestDataFactory.createInvalidRegisterRequest();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_VALIDATION_ERROR))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(4));
    }

    @Test
    void givenValidLoginRequest_whenLogin_thenReturnsOk() throws Exception {
        // Arrange
        LoginResult mockLoginResult = new LoginResult(testUser, "jwt-token");
        when(userMapper.userToUserDTO(mockLoginResult.getUser())).thenReturn(testUserDTO);

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockLoginResult);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessages.LOGIN_SUCCESS))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(testUserDTO.getId()))
                .andExpect(jsonPath("$.user.name").value(testUserDTO.getName()))
                .andExpect(jsonPath("$.user.email").value(testUserDTO.getEmail()));
    }

    @Test
    void givenWrongPassword_whenLogin_thenReturnsUnauthorized() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException(ErrorMessages.TITLE_UNAUTHORIZED));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_UNAUTHORIZED))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.INVALID_CREDENTIALS))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenNonExistentUser_whenLogin_thenReturnsNotFound() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_RESOURCE_NOT_FOUND))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.USER_NOT_FOUND))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenInvalidLoginRequest_whenLogin_thenReturnsValidationErrors() throws Exception {
        // Arrange
        LoginRequestDTO invalidRequest = TestDataFactory.createInvalidLoginRequest();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_VALIDATION_ERROR))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.validationErrors").exists());
    }
}