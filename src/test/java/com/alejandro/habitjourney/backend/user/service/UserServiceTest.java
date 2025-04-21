package com.alejandro.habitjourney.backend.user.service;

import com.alejandro.habitjourney.backend.common.exception.InvalidCredentialsException;
import com.alejandro.habitjourney.backend.common.exception.UserNotFoundException;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
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

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private UserDTO testUserDTO;
    private RegisterRequestDTO testRegisterDTO;

    @BeforeEach
    void setUp() {
        // Arrange - Usar TestDataFactory
        testUser = TestDataFactory.createTestUser();
        testUserDTO = TestDataFactory.createTestUserDTO();
        testRegisterDTO = TestDataFactory.createValidRegisterRequest();
    }

    @Test
    void givenUsersExist_whenGetAllUsers_thenReturnsListOfUserDTOs() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUserDTO, result.get(0));
        verify(userRepository).findAll();
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void givenExistingUserId_whenGetUserById_thenReturnsUserDTO() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertEquals(testUserDTO, result);
        verify(userRepository).findById(1L);
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void givenNonExistentUserId_whenGetUserById_thenThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void givenExistingUserAndValidUpdateData_whenUpdateUser_thenUpdatesUserAndReturnsUserDTO() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser);
        when(userMapper.userToUserDTO(any(User.class))).thenReturn(testUserDTO);

        // Modificar DTO para actualización
        UserDTO updateDTO = TestDataFactory.createUpdatedUserDTO();

        // Act
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO, result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(userMapper).userToUserDTO(any(User.class));

        // Verificar los detalles del usuario actualizado guardado
        User savedUser = userCaptor.getValue();
        assertEquals(updateDTO.getName(), savedUser.getName());
        assertEquals(updateDTO.getEmail(), savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash()); // Aseguramos que la contraseña NO se modificó
    }

    @Test
    void givenNonExistentUserId_whenUpdateUser_thenThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(999L, testUserDTO);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
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

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void givenExistingUserAndCorrectCurrentPassword_whenChangePassword_thenChangesPasswordSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("currentPassword"), eq("hashedPassword"))).thenReturn(true);
        when(passwordEncoder.encode(eq("NewPassword123!"))).thenReturn("newEncodedPassword");

        // Act
        userService.changePassword(1L, "currentPassword", "NewPassword123!");

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(eq("currentPassword"), eq("hashedPassword"));
        verify(passwordEncoder).encode(eq("NewPassword123!"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenExistingUserAndIncorrectCurrentPassword_whenChangePassword_thenThrowsInvalidCredentialsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            userService.changePassword(1L, "wrongPassword", "NewPassword123!");
        });

        assertEquals("La contraseña actual es incorrecta", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", testUser.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }
}