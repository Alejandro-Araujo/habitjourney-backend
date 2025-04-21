package com.alejandro.habitjourney.backend.auth.controller;

import com.alejandro.habitjourney.backend.auth.dto.*;
import com.alejandro.habitjourney.backend.auth.service.AuthService;
import com.alejandro.habitjourney.backend.common.config.SecurityConfig;
import com.alejandro.habitjourney.backend.common.config.SecurityTestConfig;
import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import({SecurityTestConfig.class})
@WebMvcTest(controllers = AuthController.class)
@ComponentScan(basePackages = "com.alejandro.habitjourney.backend.auth.controller")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthController authController; //

    @MockitoBean
    private AuthService authService;

    private RegisterRequestDTO validRegisterRequest;
    private LoginRequestDTO validLoginRequest;
    private UserDTO testUserDTO;
    private RegisterResponseDTO registerResponseDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequestDTO("Test User", "test@example.com", "Valid1Password!");
        validLoginRequest = new LoginRequestDTO("test@example.com", "Valid1Password!");

        testUserDTO = new UserDTO(1L, "Test User", "test@example.com");

        registerResponseDTO = new RegisterResponseDTO("Usuario registrado con éxito", testUserDTO);
        loginResponseDTO = new LoginResponseDTO("Login exitoso", "jwt-token", testUserDTO);

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerUser_WithValidData_ShouldReturnCreatedStatus() throws Exception {
        when(authService.registerUser(any(RegisterRequestDTO.class))).thenReturn(registerResponseDTO);

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
    void registerUser_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException(ErrorMessages.EMAIL_EXISTS));

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
    void registerUser_WithInvalidPassword_ShouldReturnBadRequest() throws Exception {
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new InvalidPasswordException(ErrorMessages.INVALID_PASSWORD));

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
    void registerUser_WithInvalidName_ShouldReturnBadRequest() throws Exception {
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new InvalidNameException(ErrorMessages.INVALID_NAME));

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
    void registerUser_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {
        when(authService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("El email no es válido"));

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
    void registerUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO("", "invalid-email", "weak");

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
    void loginUser_WithValidCredentials_ShouldReturnOkStatus() throws Exception {
        when(authService.authenticateUser(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

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
    void loginUser_WithBadCredentials_ShouldReturnUnauthorized() throws Exception {
        when(authService.authenticateUser(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

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
    void loginUser_WithUserNotFound_ShouldReturnNotFound() throws Exception {
        when(authService.authenticateUser(any(LoginRequestDTO.class)))
                .thenThrow(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

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
    void loginUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO("bad-email", "");

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