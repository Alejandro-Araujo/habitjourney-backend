package com.alejandro.habitjourney.backend.user.service;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.exception.UserNotFoundException;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;
import com.alejandro.habitjourney.backend.common.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase de servicio {@link UserService}.
 * Se enfoca en verificar la lógica de negocio relacionada con
 * la gestión de usuarios (CRUD y cambio de contraseña), interactuando
 * con sus dependencias mockeadas (repositorio, encoder).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Arrange
        testUser = TestDataFactory.createTestUser();
    }

    @Test
    void givenUsersExist_whenGetAllUsers_thenReturnsListOfUserEntities() {
        // Arrange
        List<User> users = Arrays.asList(testUser, TestDataFactory.createTestUser());
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(users.get(0).getId(), result.get(0).getId());
        assertEquals(users.get(1).getEmail(), result.get(1).getEmail());

        verify(userRepository).findAll();
    }

    @Test
    void givenExistingUserId_whenGetUserById_thenReturnsUserDTO() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void givenNonExistentUserId_whenGetUserById_thenThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void givenExistingUserAndValidUpdateData_whenUpdateUser_thenUpdatesUserAndReturnsUpdatedUserEntity() {
        // Arrange
        UserDTO updateDTO = TestDataFactory.createUpdatedUserDTO();

        when(userRepository.findById(eq(testUser.getId()))).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateDTO.getEmail())).thenReturn(false); // Asegúrate de mockear esto si tu lógica lo usa
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(testUser.getId(), updateDTO);

        // Assert
        assertNotNull(result);
        assertSame(testUser, result);
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getEmail(), result.getEmail());
        assertEquals("hashedPassword", result.getPasswordHash());

        // Verify
        verify(userRepository).findById(eq(testUser.getId()));
        verify(userRepository).save(userCaptor.capture());

        // Verificar los detalles del usuario actualizado guardado
        User savedUser = userCaptor.getValue();
        assertEquals(updateDTO.getName(), savedUser.getName());
        assertEquals(updateDTO.getEmail(), savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash());
    }

    @Test
    void givenNonExistentUserId_whenUpdateUser_thenThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(999L, TestDataFactory.createUpdatedUserDTO());
        });

        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findById(999L);
        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void givenExistingUserId_whenDeleteUser_thenDeletesUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void givenNonExistentUserId_whenDeleteUser_thenThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void givenExistingUserAndCorrectCurrentPassword_whenChangePassword_thenChangesPasswordSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("currentPassword"), eq("hashedPassword"))).thenReturn(true);
        when(passwordEncoder.encode(eq("newValidPassword123!"))).thenReturn("newEncodedPassword");

        // Act
        userService.changePassword(1L, "currentPassword", "newValidPassword123!");

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(eq("currentPassword"), eq("hashedPassword"));
        verify(passwordEncoder).encode(eq("newValidPassword123!"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenExistingUserAndIncorrectCurrentPassword_whenChangePassword_thenThrowsBadCredentialsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.changePassword(1L, "wrongPassword", "newValidPassword123!");
        });

        assertEquals(ErrorMessages.CURRENT_PASSWORD_INCORRECT, exception.getMessage());
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", testUser.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }
}