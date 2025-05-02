package com.alejandro.habitjourney.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de inicio de sesión.
 * Contiene las credenciales necesarias para autenticar al usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de inicio de sesión del usuario")
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser una dirección de email válida")
    @Schema(description = "Email del usuario", example = "usuario@ejemplo.com")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña del usuario", example = "Contraseña123!")
    private String password;
}
