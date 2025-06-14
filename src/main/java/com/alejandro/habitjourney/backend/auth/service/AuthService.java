package com.alejandro.habitjourney.backend.auth.service;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.security.JwtUtil;
import com.alejandro.habitjourney.backend.common.util.ValidationUtils;
import com.alejandro.habitjourney.backend.auth.dto.LoginRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio que gestiona la autenticación y registro de usuarios.
 * Maneja la lógica de negocio relacionada con la autenticación.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario después de validar sus datos.
     *
     * @param registerRequestDTO DTO con nombre, email y contraseña
     * @return DTO con mensaje y datos del usuario registrado
     * @throws EmailAlreadyExistsException si el email ya está registrado
     * @throws InvalidEmailFormatException si el email no cumple con el formato válido
     * @throws InvalidPasswordException    si la contraseña no cumple con los requisitos
     * @throws InvalidNameException        si el nombre no es válido
     */
    @Transactional
    public User register(RegisterRequestDTO registerRequestDTO) {
        log.debug("Iniciando proceso de registro para: {}", registerRequestDTO.getEmail());
        SecurityContextHolder.clearContext();
        // Validaciones
        String emailValidation = ValidationUtils.validateEmail(registerRequestDTO.getEmail());
        if (emailValidation != null) {
            log.warn("Validación de email fallida: {}", emailValidation);
            throw new InvalidEmailFormatException(emailValidation);
        }

        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            log.warn("Email ya registrado: {}", registerRequestDTO.getEmail());
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_EXISTS);
        }

        String passwordValidation = ValidationUtils.validatePassword(registerRequestDTO.getPassword());
        if (passwordValidation != null) {
            log.warn("Validación de contraseña fallida para: {}", registerRequestDTO.getEmail());
            throw new InvalidPasswordException(passwordValidation);
        }

        String nameValidation = ValidationUtils.validateName(registerRequestDTO.getName());
        if (nameValidation != null) {
            log.warn("Validación de nombre fallida para: {}", registerRequestDTO.getEmail());
            throw new InvalidNameException(nameValidation);
        }

        // Crear y guardar usuario
        User user = new User();
        user.setName(registerRequestDTO.getName());
        user.setEmail(registerRequestDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        log.info("Usuario registrado correctamente: {}", user.getEmail());

        return user;
    }

    /**
     * Autentica un usuario con sus credenciales y genera un token JWT.
     *
     * @param loginRequestDTO DTO con email y contraseña
     * @return DTO con token JWT y datos del usuario
     * @throws UserNotFoundException si no se encuentra el usuario
     */
    public LoginResult login(LoginRequestDTO loginRequestDTO) {
        log.debug("Iniciando proceso de login para: {}", loginRequestDTO.getEmail());

        boolean exists = userRepository.existsByEmail(loginRequestDTO.getEmail());
        if (!exists) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }
        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Buscar usuario
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Generar token
        String token = jwtUtil.generateAccessToken(authentication);

        log.info("Login exitoso para usuario: {}", user.getEmail());

        // Retornar respuesta
        return new LoginResult(user, token);
    }

    /**
     * Obtiene el usuario autenticado actualmente en el sistema.
     *
     * @return Usuario autenticado
     * @throws UserNotFoundException si no se encuentra el usuario en el sistema
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Obteniendo usuario autenticado: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));
    }

    // Clase auxiliar interna para devolver el resultado del login
    @Data
    @AllArgsConstructor
    public static class LoginResult {
        private User user;
        private String token;
    }
}