package com.alejandro.habitjourney.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de registro de nuevo usuario.
 * Incluye validaciones para asegurar datos correctos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para registrar un nuevo usuario")
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe contener entre 2 y 50 caracteres")
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser una dirección de email válida")
    @Schema(description = "Email del usuario (usado como identificador)", example = "usuario@ejemplo.com")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,32}$",
            message = "La contraseña debe tener entre 6 y 32 caracteres, incluyendo al menos una mayúscula, una minúscula, un número y un carácter especial")
    @Schema(
            description = "Contraseña del usuario (mín. 6 caracteres, máx. 32, con mayúsculas, minúsculas, números y caracteres especiales)",
            example = "Pass123!"
    )
    private String password;
}