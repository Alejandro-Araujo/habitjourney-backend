package com.alejandro.habitjourney.backend.auth.controller;

import com.alejandro.habitjourney.backend.auth.dto.LoginRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.LoginResponseDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterResponseDTO;
import com.alejandro.habitjourney.backend.auth.service.AuthService;
import com.alejandro.habitjourney.backend.auth.service.AuthService.LoginResult;
import com.alejandro.habitjourney.backend.common.constant.SuccessMessages;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para la autenticación de usuarios.
 * Expone los endpoints públicos de registro y login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints públicos para registrar e iniciar sesión de usuarios")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param registerRequestDTO datos del usuario (nombre, email, contraseña)
     * @return información del usuario creado + mensaje de éxito
     */
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("Solicitud de registro para usuario: {}", registerRequestDTO.getEmail());
        SecurityContextHolder.clearContext();
        User registeredUser = authService.register(registerRequestDTO);
        UserDTO userDTO = userMapper.userToUserDTO(registeredUser);
        log.info("Usuario registrado correctamente: {}", registerRequestDTO.getEmail());
        RegisterResponseDTO response = RegisterResponseDTO.builder()
                .message(SuccessMessages.USER_REGISTERED_SUCCESS)
                .user(userDTO)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * @param loginRequestDTO credenciales del usuario (email, contraseña)
     * @return token JWT y datos del usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Petición incorrecta"),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {

        log.info("Intento de login para usuario: {}", loginRequestDTO.getEmail());
        LoginResult loginResult = authService.login(loginRequestDTO);
        UserDTO userDTO = userMapper.userToUserDTO(loginResult.getUser());
        log.info("Login exitoso para usuario: {}", loginRequestDTO.getEmail());
        LoginResponseDTO response = LoginResponseDTO.builder()
                .message(SuccessMessages.LOGIN_SUCCESS)
                .token(loginResult.getToken())
                .user(userDTO)
                .build();
        return ResponseEntity.ok(response);
    }
}
