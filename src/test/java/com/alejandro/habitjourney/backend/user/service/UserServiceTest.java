package com.alejandro.habitjourney.backend.user.service;

import com.alejandro.habitjourney.backend.common.exception.EmailAlreadyExistsException;
import com.alejandro.habitjourney.backend.common.exception.InvalidCredentialsException;
import com.alejandro.habitjourney.backend.common.exception.UserNotFoundException;
import com.alejandro.habitjourney.backend.user.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;
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
        // Configurar datos de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setName("Test User");
        testUserDTO.setEmail("test@example.com");

        testRegisterDTO = new RegisterRequestDTO();
        testRegisterDTO.setName("New User");
        testRegisterDTO.setEmail("new@example.com");
        testRegisterDTO.setPassword("Password123!");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Configurar mock
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Ejecutar método a probar
        List<UserDTO> result = userService.getAllUsers();

        // Verificar resultado
        assertEquals(1, result.size());
        assertEquals(testUserDTO, result.get(0));
        verify(userRepository).findAll();
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Configurar mock
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Ejecutar método a probar
        UserDTO result = userService.getUserById(1L);

        // Verificar resultado
        assertEquals(testUserDTO, result);
        verify(userRepository).findById(1L);
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        // Configurar mock
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción correcta
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void registerUser_WhenEmailNotExists_ShouldRegisterUser() {
        // Configurar mocks
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(eq("Password123!"))).thenReturn("encodedPassword");
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser); // Capturamos el User antes de retornar el mock
        when(userMapper.userToUserDTO(any(User.class))).thenReturn(testUserDTO);

        // Ejecutar método a probar
        UserDTO result = userService.registerUser(testRegisterDTO);

        // Verificar resultado
        assertNotNull(result);
        assertEquals(testUserDTO, result);
        verify(userRepository).existsByEmail(testRegisterDTO.getEmail());
        verify(passwordEncoder).encode(testRegisterDTO.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userMapper).userToUserDTO(any(User.class));

        // Verificar los detalles del usuario guardado
        User savedUser = userCaptor.getValue();
        assertEquals(testRegisterDTO.getName(), savedUser.getName());
        assertEquals(testRegisterDTO.getEmail(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPasswordHash());
    }

    @Test
    void registerUser_WhenEmailExists_ShouldThrowException() {
        // Configurar mock
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Verificar que se lanza la excepción correcta
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.registerUser(testRegisterDTO);
        });

        assertEquals("El email ya está en uso", exception.getMessage());
        verify(userRepository).existsByEmail(testRegisterDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateUser() {
        // Configurar mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser); // Capturamos el User antes de retornar el mock
        when(userMapper.userToUserDTO(any(User.class))).thenReturn(testUserDTO);

        // Modificar DTO para actualización
        UserDTO updateDTO = new UserDTO();
        updateDTO.setId(1L);
        updateDTO.setName("Updated Name");
        updateDTO.setEmail("updated@example.com");

        // Ejecutar método a probar
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Verificar resultado
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
    void updateUser_WhenUserNotExists_ShouldThrowException() {
        // Configurar mock
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción correcta
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(999L, testUserDTO);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Configurar mock
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Ejecutar método a probar
        userService.deleteUser(1L);

        // Verificar que se llamó al repositorio
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        // Configurar mock
        when(userRepository.existsById(999L)).thenReturn(false);

        // Verificar que se lanza la excepción correcta
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void changePassword_WhenValidCredentials_ShouldChangePassword() {
        // Configurar mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("currentPassword"), eq("hashedPassword"))).thenReturn(true);
        when(passwordEncoder.encode(eq("NewPassword123!"))).thenReturn("anyEncodedPassword"); // No nos importa el valor exacto

        // Ejecutar método a probar
        userService.changePassword(1L, "currentPassword", "NewPassword123!");

        // Verificar interacciones
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(eq("currentPassword"), eq("hashedPassword"));
        verify(passwordEncoder).encode(eq("NewPassword123!"));
        verify(userRepository).save(any(User.class)); // Verificamos que se llamó save con CUALQUIER User
    }

    @Test
    void changePassword_WhenIncorrectCurrentPassword_ShouldThrowException() {
        // Configurar mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Verificar que se lanza la excepción correcta
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            userService.changePassword(1L, "wrongPassword", "NewPassword123!");
        });

        assertEquals("La contraseña actual es incorrecta", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", testUser.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validatePassword_WhenValidFormat_ShouldReturnTrue() {
        // Probar una contraseña válida
        boolean result = userService.validatePassword("Valid1Password!");

        // Verificar resultado
        assertTrue(result);
    }

    @Test
    void validatePassword_WhenInvalidFormat_ShouldReturnFalse() {
        // Probar casos inválidos
        assertFalse(userService.validatePassword(null), "Null password should be invalid");
        assertFalse(userService.validatePassword("short"), "Too short password should be invalid");
        assertFalse(userService.validatePassword("nouppercase123!"), "Password without uppercase should be invalid");
        assertFalse(userService.validatePassword("NOLOWERCASE123!"), "Password without lowercase should be invalid");
        assertFalse(userService.validatePassword("NoNumbers!"), "Password without numbers should be invalid");
        assertFalse(userService.validatePassword("NoSpecialChars123"), "Password without special chars should be invalid");
    }
}
