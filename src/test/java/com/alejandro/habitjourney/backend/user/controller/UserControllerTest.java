package com.alejandro.habitjourney.backend.user.controller;

import com.alejandro.habitjourney.backend.auth.service.AuthService;
import com.alejandro.habitjourney.backend.common.config.SecurityTestConfig;
import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.constant.SuccessMessages;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.config.TestConfig;
import com.alejandro.habitjourney.backend.common.security.JwtUtil;
import com.alejandro.habitjourney.backend.common.util.TestDataFactory;
import com.alejandro.habitjourney.backend.user.dto.PasswordChangeDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.model.User;
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

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User testUser;
    private UserDTO testUserDTO;
    private PasswordChangeDTO validPasswordChangeDTO;
    private User updatedUserEntity;
    private UserDTO updatedUserDTO;

    @BeforeEach
    void setUp() {
        // Arrange - Usar TestDataFactory
        testUserDTO = TestDataFactory.createTestUserDTO();
        validPasswordChangeDTO = TestDataFactory.createValidPasswordChangeDTO();
        testUser = TestDataFactory.createTestUser();
        updatedUserEntity = TestDataFactory.createTestUser();
        updatedUserEntity.setName("Updated Name");
        updatedUserEntity.setEmail("updated@example.com");
        updatedUserDTO = TestDataFactory.createUpdatedUserDTO();
    }

    @Test
    void givenAuthenticatedUser_whenGetCurrentUser_thenReturnsOkAndUserData() throws Exception {
        // Arrange
        when(authService.getAuthenticatedUser()).thenReturn(testUser);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_FOUND_SUCCESS))
                .andExpect(jsonPath("$.user.id").value(testUserDTO.getId()))
                .andExpect(jsonPath("$.user.name").value(testUserDTO.getName()))
                .andExpect(jsonPath("$.user.email").value(testUserDTO.getEmail()));

        verify(authService).getAuthenticatedUser();
        verify(userMapper).userToUserDTO(testUser);
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void givenAuthenticatedUserAndValidUpdateData_whenUpdateUser_thenReturnsOkAndUpdatedUser() throws Exception {
        // Arrange
        when(authService.getAuthenticatedUser()).thenReturn(testUser);
        when(userService.updateUser(eq(testUser.getId()), eq(updatedUserDTO))).thenReturn(updatedUserEntity);
        when(userMapper.userToUserDTO(updatedUserEntity)).thenReturn(updatedUserDTO);

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_UPDATED_SUCCESS))
                .andExpect(jsonPath("$.user.id").value(updatedUserDTO.getId()))
                .andExpect(jsonPath("$.user.name").value(updatedUserDTO.getName()))
                .andExpect(jsonPath("$.user.email").value(updatedUserDTO.getEmail()));

        verify(authService).getAuthenticatedUser();
        verify(userService).updateUser(eq(testUser.getId()), eq(updatedUserDTO));
        verify(userMapper).userToUserDTO(updatedUserEntity);
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void givenAuthenticatedUser_whenDeleteUser_thenReturnsOkAndSuccessMessage() throws Exception {
        // Arrange
        when(authService.getAuthenticatedUser()).thenReturn(testUser);
        doNothing().when(userService).deleteUser(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_DELETED_SUCCESS));

        verify(authService).getAuthenticatedUser();
        verify(userService).deleteUser(eq(testUser.getId()));
        verify(userService, never()).getUserByEmail(anyString());
        verify(userMapper, never()).userToUserDTO(any());
    }

    @Test
    void givenAuthenticatedUserAndValidPasswordChangeData_whenChangePassword_thenReturnsOkAndSuccessMessage() throws Exception {
        // Arrange
        when(authService.getAuthenticatedUser()).thenReturn(testUser);
        doNothing().when(userService).changePassword(anyLong(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/users/me/change-password")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessages.PASSWORD_CHANGED_SUCCESS));

        verify(authService).getAuthenticatedUser();
        verify(userService).changePassword(eq(testUser.getId()), eq(validPasswordChangeDTO.getCurrentPassword()), eq(validPasswordChangeDTO.getNewPassword())); // Verifica llamada a userService con ID y passwords
        verify(userService, never()).getUserByEmail(anyString());
        verify(userMapper, never()).userToUserDTO(any());
    }

    @Test
    void givenAuthenticatedUserAndUpdateWithExistingEmail_whenUpdateUser_thenReturnsBadRequestAndEmailExistsError() throws Exception {
        // Arrange
        UserDTO updateRequestDTO = TestDataFactory.createUpdatedUserDTO();
        updateRequestDTO.setEmail("test@example.com");

        when(authService.getAuthenticatedUser()).thenReturn(testUser);
        when(userService.updateUser(anyLong(), any(UserDTO.class)))
                .thenThrow(new EmailAlreadyExistsException(ErrorMessages.EMAIL_EXISTS));

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_CONFLICT))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.EMAIL_EXISTS))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).getAuthenticatedUser();
        verify(userService).updateUser(eq(testUser.getId()), any(UserDTO.class));
        verify(userMapper, never()).userToUserDTO(any());
    }

    @Test
    void givenAuthenticatedUserAndIncorrectCurrentPassword_whenChangePassword_thenReturnsBadRequestAndInvalidCredentialsError() throws Exception {
        // Arrange
        when(authService.getAuthenticatedUser()).thenReturn(testUser);
        doThrow(new InvalidPasswordException(ErrorMessages.INVALID_PASSWORD))
                .when(userService).changePassword(anyLong(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/users/me/change-password")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_BAD_REQUEST))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.INVALID_PASSWORD))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).getAuthenticatedUser();
        verify(userService).changePassword(eq(testUser.getId()), anyString(), anyString());
        verify(userMapper, never()).userToUserDTO(any());
    }

    @Test
    void givenAuthenticatedUserAndInvalidNewPasswordFormat_whenChangePassword_thenReturnsBadRequestAndInvalidPasswordError() throws Exception {
        // Arrange
        when(authService.getAuthenticatedUser()).thenReturn(testUser);
        doThrow(new InvalidPasswordException(ErrorMessages.INVALID_PASSWORD))
                .when(userService).changePassword(anyLong(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/users/me/change-password")
                        .with(authentication(createTestAuthentication("test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_BAD_REQUEST))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.INVALID_PASSWORD))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).getAuthenticatedUser();
        verify(userService).changePassword(eq(testUser.getId()), anyString(), anyString());
        verify(userMapper, never()).userToUserDTO(any());
    }

    @Test
    void givenAuthenticatedUserDoesNotExist_whenGetCurrentUser_thenReturnsNotFoundAndUserNotFoundError() throws Exception {
        // Arrange
        when(authService.getAuthenticatedUser())
                .thenThrow(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(createTestAuthentication("nonexistent@example.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value(ErrorMessages.TITLE_RESOURCE_NOT_FOUND))
                .andExpect(jsonPath("$.detail").value(ErrorMessages.USER_NOT_FOUND))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).getAuthenticatedUser();
        verifyNoInteractions(userService);
        verifyNoInteractions(userMapper);
    }
}