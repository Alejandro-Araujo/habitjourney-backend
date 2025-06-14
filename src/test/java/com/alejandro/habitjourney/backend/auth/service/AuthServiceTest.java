package com.alejandro.habitjourney.backend.auth.service;


import com.alejandro.habitjourney.backend.auth.service.AuthService.LoginResult;
import com.alejandro.habitjourney.backend.auth.dto.LoginRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.security.JwtUtil;
import com.alejandro.habitjourney.backend.common.config.TestConfig;
import com.alejandro.habitjourney.backend.common.util.TestDataFactory;
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

    @BeforeEach
    void setUp() {
        // Arrange - Usar TestDataFactory
        validRegisterRequest = TestDataFactory.createValidRegisterRequest();
        validLoginRequest = TestDataFactory.createValidLoginRequest();
        testUser = TestDataFactory.createTestUser();
    }

    @Test
    void givenValidRegisterRequest_whenRegisterUser_thenRegistersSuccessfullyAndReturnsUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());

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
                () -> authService.register(validRegisterRequest)
        );

        assertEquals(ErrorMessages.EMAIL_EXISTS, exception.getMessage());
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
        InvalidEmailFormatException exception = assertThrows(
                InvalidEmailFormatException.class,
                () -> authService.register(invalidEmailRequest)
        );

        assertEquals(ErrorMessages.VALIDATION_EMAIL_FORMAT, exception.getMessage());
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
                () -> authService.register(invalidPasswordRequest)
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
                () -> authService.register(emptyNameRequest)
        );

        assertEquals("El nombre no puede estar vacío", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenValidLoginRequest_whenLogin_thenReturnsTokenAndUser() {
        // Arrange
        // --- CAMBIO CLAVE: Primero simulamos que el usuario existe ---
        when(userRepository.existsByEmail(validLoginRequest.getEmail())).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(authentication)).thenReturn("jwt-token");

        // Act
        LoginResult result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(testUser.getEmail(), result.getUser().getEmail());
        assertEquals("jwt-token", result.getToken());

        // Verificamos la nueva llamada
        verify(userRepository).existsByEmail(validLoginRequest.getEmail());
        verify(authenticationManager).authenticate(
                argThat(auth ->
                        validLoginRequest.getEmail().equals(auth.getPrincipal()) &&
                                validLoginRequest.getPassword().equals(auth.getCredentials())
                )
        );
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verify(jwtUtil).generateAccessToken(authentication);
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenThrowsBadCredentialsException() {
        // Arrange
        // --- CAMBIO CLAVE: Simulamos que el usuario existe para pasar la primera validación ---
        when(userRepository.existsByEmail(validLoginRequest.getEmail())).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(ErrorMessages.INVALID_CREDENTIALS));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(validLoginRequest)
        );

        assertEquals(ErrorMessages.INVALID_CREDENTIALS, exception.getMessage());
        verify(userRepository).existsByEmail(validLoginRequest.getEmail()); // Se verifica la llamada
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    void givenLoginRequestForNonExistentUser_whenLogin_thenThrowsUserNotFoundException() {
        // Arrange
        // --- ESTE ES EL TEST PARA LA NUEVA LÓGICA ---
        when(userRepository.existsByEmail(validLoginRequest.getEmail())).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.login(validLoginRequest)
        );

        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        // Verificamos que se queda en la primera validación y no sigue
        verify(userRepository).existsByEmail(validLoginRequest.getEmail());
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    void givenAuthenticatedUserExists_whenGetAuthenticatedUser_thenReturnsUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Configurar SecurityContextHolder
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

        // Configurar SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.getAuthenticatedUser()
        );

        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
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
