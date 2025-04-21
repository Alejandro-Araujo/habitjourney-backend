package com.alejandro.habitjourney.backend.auth.service;


import com.alejandro.habitjourney.backend.auth.dto.LoginRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.LoginResponseDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterResponseDTO;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.security.JwtUtil;
import com.alejandro.habitjourney.backend.common.config.TestConfig;
import com.alejandro.habitjourney.backend.common.util.TestDataFactory;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase de servicio {@link AuthService}.
 * Se enfoca en verificar la lógica de negocio relacionada con
 * el registro y la autenticación de usuarios, interactuando
 * con sus dependencias mockeadas (repositorios, encoders, etc.).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest extends TestConfig {

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
        // Arrange - Usar TestDataFactory
        validRegisterRequest = TestDataFactory.createValidRegisterRequest();
        validLoginRequest = TestDataFactory.createValidLoginRequest();
        testUser = TestDataFactory.createTestUser();
        testUserDTO = TestDataFactory.createTestUserDTO();
    }

    @Test
    void givenValidRegisterRequest_whenRegisterUser_thenRegistersSuccessfullyAndReturnsUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.userToUserDTO(any(User.class))).thenReturn(testUserDTO);

        // Act
        RegisterResponseDTO result = authService.registerUser(validRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Usuario registrado con éxito", result.getMessage());
        assertNotNull(result.getUser());
        assertEquals(testUserDTO.getId(), result.getUser().getId());
        assertEquals(testUserDTO.getName(), result.getUser().getName());
        assertEquals(testUserDTO.getEmail(), result.getUser().getEmail());

        verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
        verify(passwordEncoder).encode(validRegisterRequest.getPassword());
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(validRegisterRequest.getName(), savedUser.getName());
        assertEquals(validRegisterRequest.getEmail(), savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash());
    }

    @Test
    void givenExistingEmail_whenRegisterUser_thenThrowsEmailAlreadyExistsException() {
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
    void givenInvalidEmailFormat_whenRegisterUser_thenThrowsInvalidCredentialsException() {
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
    void givenInvalidPasswordFormat_whenRegisterUser_thenThrowsInvalidPasswordException() {
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
    void givenEmptyName_whenRegisterUser_thenThrowsInvalidNameException() {
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
    void givenValidLoginRequest_whenAuthenticateUser_thenReturnsTokenAndUser() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(authentication)).thenReturn("jwt-token");
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act
        LoginResponseDTO result = authService.authenticateUser(validLoginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Login exitoso", result.getMessage());
        assertEquals("jwt-token", result.getToken());
        assertEquals(testUserDTO, result.getUser());

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
    void givenInvalidCredentials_whenAuthenticateUser_thenThrowsBadCredentialsException() {
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
    void givenLoginRequestForNonExistentUser_whenAuthenticateUser_thenThrowsUserNotFoundException() {
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
    void givenAuthenticatedUserExists_whenGetAuthenticatedUser_thenReturnsUser() {
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
    void givenAuthenticatedUserNotFound_whenGetAuthenticatedUser_thenThrowsUserNotFoundException() {
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