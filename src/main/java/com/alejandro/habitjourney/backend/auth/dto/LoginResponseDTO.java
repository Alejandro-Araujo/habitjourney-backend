package com.alejandro.habitjourney.backend.auth.dto;


import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de inicio de sesión exitoso.
 * Contiene el token JWT generado y los datos básicos del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de login exitoso con token JWT")
public class LoginResponseDTO {

    @Schema(description = "Mensaje informativo", example = "Login exitoso")
    private String message;

    @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Datos del usuario autenticado")
    private UserDTO user;
}