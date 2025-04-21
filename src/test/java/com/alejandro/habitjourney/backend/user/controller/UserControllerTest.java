package com.alejandro.habitjourney.backend.user.controller;


import com.alejandro.habitjourney.backend.common.config.SecurityTestConfig;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.config.TestConfig;
import com.alejandro.habitjourney.backend.common.util.TestDataFactory;
import com.alejandro.habitjourney.backend.user.dto.PasswordChangeDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de slice (@WebMvcTest) para la clase de controlador {@link UserController}.
 * Se enfoca en verificar el manejo de las solicitudes HTTP,
 * las respuestas y la correcta integración con el servicio {@link UserService} (mockeado).
 * Configura un contexto mínimo de Spring MVC para las pruebas.
 */
@WebMvcTest
@ContextConfiguration(classes = {SecurityTestConfig.class, UserControllerTestConfig.class})
class UserControllerTest extends TestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserDTO testUserDTO;
    private PasswordChangeDTO validPasswordChangeDTO;

    @BeforeEach
    void setUp() {
        // Arrange - Usar TestDataFactory
        testUserDTO = TestDataFactory.createTestUserDTO();
        validPasswordChangeDTO = TestDataFactory.createValidPasswordChangeDTO();
    }

    @Test
    void givenAuthenticatedUser_whenGetCurrentUser_thenReturnsOkAndUserData() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario encontrado"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void givenAuthenticatedUserAndValidUpdateData_whenUpdateUser_thenReturnsOkAndUpdatedUser() throws Exception {
        // Arrange
        UserDTO updatedUser = TestDataFactory.createUpdatedUserDTO();

        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        when(userService.updateUser(anyLong(), any(UserDTO.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario actualizado correctamente"))
                .andExpect(jsonPath("$.user.name").value("Updated Name"));
    }

    @Test
    void givenAuthenticatedUser_whenDeleteUser_thenReturnsOkAndSuccessMessage() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        doNothing().when(userService).deleteUser(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cuenta eliminada correctamente"));
    }

    @Test
    void givenAuthenticatedUserAndValidPasswordChangeData_whenChangePassword_thenReturnsOkAndSuccessMessage() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        doNothing().when(userService).changePassword(anyLong(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/users/me/change-password")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente"));
    }

    @Test
    void givenAuthenticatedUserAndUpdateWithExistingEmail_whenUpdateUser_thenReturnsBadRequestAndEmailExistsError() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        when(userService.updateUser(anyLong(), any(UserDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("Ya existe un usuario con este correo electrónico"));

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Registro inválido"))
                .andExpect(jsonPath("$.detail").value("Ya existe un usuario con este correo electrónico"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenAuthenticatedUserAndIncorrectCurrentPassword_whenChangePassword_thenReturnsBadRequestAndInvalidCredentialsError() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        doThrow(new InvalidCredentialsException("La contraseña actual es incorrecta"))
                .when(userService).changePassword(anyLong(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/users/me/change-password")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Credenciales inválidas"));
    }

    @Test
    void givenAuthenticatedUserAndInvalidNewPasswordFormat_whenChangePassword_thenReturnsBadRequestAndInvalidPasswordError() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
        doThrow(new InvalidPasswordException("La contraseña debe tener entre 6 y 32 caracteres"))
                .when(userService).changePassword(anyLong(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/users/me/change-password")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Contraseña inválida"))
                .andExpect(jsonPath("$.detail").value("La contraseña debe tener entre 6 y 32 caracteres"));
    }

    @Test
    void givenAuthenticatedUserDoesNotExist_whenGetCurrentUser_thenReturnsNotFoundAndUserNotFoundError() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString()))
                .thenThrow(new UserNotFoundException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(createTestAuthentication("nonexistent@example.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.detail").value("Usuario no encontrado"));
    }
}