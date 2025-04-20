package com.alejandro.habitjourney.backend.auth.service;

import com.alejandro.habitjourney.backend.auth.dto.LoginRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.security.JwtUtil;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private RegisterRequestDTO validRegisterRequest;
    private LoginRequestDTO validLoginRequest;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba para registro
        validRegisterRequest = new RegisterRequestDTO();
        validRegisterRequest.setName("Test User");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("Valid1Password!");

        // Configurar datos de prueba para login
        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("Valid1Password!");

        // Configurar usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");

        // Configurar DTO de usuario
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setName("Test User");
        testUserDTO.setEmail("test@example.com");
    }

    @Test
    void registerUser_WithValidData_ShouldRegisterSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.userToUserDTO(any(User.class))).thenReturn(testUserDTO);

        // Act
        UserDTO result = authService.registerUser(validRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getName(), result.getName());
        assertEquals(testUserDTO.getEmail(), result.getEmail());

        verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
        verify(passwordEncoder).encode(validRegisterRequest.getPassword());
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(validRegisterRequest.getName(), savedUser.getName());
        assertEquals(validRegisterRequest.getEmail(), savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash());
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.registerUser(validRegisterRequest)
        );

        assertEquals("El email ya está en uso", exception.getMessage());
        verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO invalidEmailRequest = new RegisterRequestDTO();
        invalidEmailRequest.setName("Test User");
        invalidEmailRequest.setEmail("invalid-email");
        invalidEmailRequest.setPassword("Valid1Password!");

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.registerUser(invalidEmailRequest)
        );

        assertEquals("El formato del email no es válido", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO invalidPasswordRequest = new RegisterRequestDTO();
        invalidPasswordRequest.setName("Test User");
        invalidPasswordRequest.setEmail("test@example.com");
        invalidPasswordRequest.setPassword("weak");

        // Act & Assert
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> authService.registerUser(invalidPasswordRequest)
        );

        assertEquals("La contraseña debe tener al menos 6 caracteres", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithEmptyName_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO emptyNameRequest = new RegisterRequestDTO();
        emptyNameRequest.setName("");
        emptyNameRequest.setEmail("test@example.com");
        emptyNameRequest.setPassword("Valid1Password!");

        // Act & Assert
        InvalidNameException exception = assertThrows(
                InvalidNameException.class,
                () -> authService.registerUser(emptyNameRequest)
        );

        assertEquals("El nombre no puede estar vacío", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnTokenAndUser() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(authentication)).thenReturn("jwt-token");
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act
        Map<String, Object> result = authService.authenticateUser(validLoginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.get("token"));
        assertEquals(testUserDTO, result.get("user"));

        verify(authenticationManager).authenticate(
                argThat(auth ->
                        validLoginRequest.getEmail().equals(auth.getPrincipal()) &&
                                validLoginRequest.getPassword().equals(auth.getCredentials())
                )
        );
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verify(jwtUtil).generateAccessToken(authentication);
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void authenticateUser_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateUser(validLoginRequest)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void authenticateUser_UserNotFound_ShouldThrowException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.authenticateUser(validLoginRequest)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
    }

    @Test
    void getAuthenticatedUser_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Configurar SecurityContextHolder para usar nuestro mock
        SecurityContextHolder.setContext(securityContext);

        // Act
        User result = authService.getAuthenticatedUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getAuthenticatedUser_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Configurar SecurityContextHolder para usar nuestro mock
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.getAuthenticatedUser()
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    // Limpiamos el SecurityContextHolder después de cada test
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}