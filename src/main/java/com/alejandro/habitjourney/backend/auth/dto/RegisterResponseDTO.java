package com.alejandro.habitjourney.backend.auth.dto;

import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de registro exitoso.
 * Contiene los datos básicos del usuario creado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de registro exitoso")
public class RegisterResponseDTO {

    @Schema(description = "Mensaje informativo", example = "Usuario registrado con éxito")
    private String message;

    @Schema(description = "Datos del usuario registrado")
    private UserDTO user;
}