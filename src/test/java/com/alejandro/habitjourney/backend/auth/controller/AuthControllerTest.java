package com.alejandro.habitjourney.backend.auth.controller;

import com.alejandro.habitjourney.backend.auth.dto.*;
import com.alejandro.habitjourney.backend.auth.service.AuthService;
import com.alejandro.habitjourney.backend.common.config.SecurityTestConfig;
import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.config.TestConfig;
import com.alejandro.habitjourney.backend.common.util.TestDataFactory;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes = {SecurityTestConfig.class, AuthControllerTestConfig.class})
@WebMvcTest
class AuthControllerTest extends TestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private RegisterRequestDTO validRegisterRequest;
    private LoginRequestDTO validLoginRequest;
    private UserDTO testUserDTO;
    private RegisterResponseDTO registerResponseDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        // Arrange - Usar TestDataFactory
        validRegisterRequest = TestDataFactory.createValidRegisterRequest();
        validLoginRequest = TestDataFactory.createValidLoginRequest();
        testUserDTO = TestDataFactory.createTestUserDTO();

        registerResponseDTO = new RegisterResponseDTO("Usuario registrado con éxito", testUserDTO);
        loginResponseDTO = new LoginResponseDTO("Login exitoso", "jwt-token", testUserDTO);
    }

    @Test
    void givenValidRegisterRequest_whenRegister_thenReturnsCreated() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequestDTO.class))).thenReturn(registerResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuario registrado con éxito"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void givenExistingEmail_whenRegister_thenReturnsBadRequest() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException(ErrorMessages.EMAIL_EXISTS));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Registro inválido"))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.EMAIL_EXISTS))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenInvalidPassword_whenRegister_thenReturnsBadRequest() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new InvalidPasswordException(ErrorMessages.INVALID_PASSWORD));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Contraseña inválida"))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.INVALID_PASSWORD))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenInvalidName_whenRegister_thenReturnsBadRequest() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new InvalidNameException(ErrorMessages.INVALID_NAME));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Nombre inválido"))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.INVALID_NAME))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenInvalidEmail_whenRegister_thenReturnsBadRequest() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("El email no es válido"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.detail").value("El email no es válido"))
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
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(4));
    }

    @Test
    void givenValidLoginRequest_whenLogin_thenReturnsOk() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void givenWrongPassword_whenLogin_thenReturnsUnauthorized() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.INVALID_CREDENTIALS))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenNonExistentUser_whenLogin_thenReturnsNotFound() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(LoginRequestDTO.class)))
                .thenThrow(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Usuario no encontrado"))
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
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.validationErrors").exists());
    }
}