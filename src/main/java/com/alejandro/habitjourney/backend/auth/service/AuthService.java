package com.alejandro.habitjourney.backend.auth.service;


import com.alejandro.habitjourney.backend.auth.dto.LoginResponseDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterResponseDTO;
import com.alejandro.habitjourney.backend.common.exception.*;
import com.alejandro.habitjourney.backend.common.security.JwtUtil;
import com.alejandro.habitjourney.backend.common.util.ValidationUtils;
import com.alejandro.habitjourney.backend.auth.dto.LoginRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public RegisterResponseDTO registerUser(RegisterRequestDTO registerRequestDTO) {
        // Validaciones
        String emailValidation = ValidationUtils.validateEmail(registerRequestDTO.getEmail());
        if (emailValidation != null) {
            throw new InvalidCredentialsException(emailValidation);
        }

        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso");
        }

        String passwordValidation = ValidationUtils.validatePassword(registerRequestDTO.getPassword());
        if (passwordValidation != null) {
            throw new InvalidPasswordException(passwordValidation);
        }

        String nameValidation = ValidationUtils.validateName(registerRequestDTO.getName());
        if (nameValidation != null) {
            throw new InvalidNameException(nameValidation);
        }

        // Crear y guardar usuario
        User user = new User();
        user.setName(registerRequestDTO.getName());
        user.setEmail(registerRequestDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user = userRepository.save(user);
        UserDTO userDTO = userMapper.userToUserDTO(user);

        // Retornar DTO de respuesta
        return RegisterResponseDTO.builder()
                .message("Usuario registrado con éxito")
                .user(userDTO)
                .build();
    }

    public LoginResponseDTO authenticateUser(LoginRequestDTO loginRequestDTO) {

        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Buscar usuario
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Generar token
        String token = jwtUtil.generateAccessToken(authentication);

        // Retornar DTO de respuesta
        return LoginResponseDTO.builder()
                .message("Login exitoso")
                .token(token)
                .user(userMapper.userToUserDTO(user))
                .build();
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    }
}
