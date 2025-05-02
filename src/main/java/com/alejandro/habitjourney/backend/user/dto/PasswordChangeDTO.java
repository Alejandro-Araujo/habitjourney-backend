package com.alejandro.habitjourney.backend.user.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para la petición de cambio de contraseña de usuario.
 * Define la estructura de los datos esperados al solicitar un cambio de contraseña.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDTO {

    @NotBlank(message = "La contraseña actual es requerida")
    @Schema(description = "Contraseña actual del usuario", example = "MiPasswordActual1!")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,32}$",
            message = "La contraseña debe tener entre 6 y 32 caracteres, incluyendo al menos una mayúscula, una minúscula, un número y un carácter especial")
    @Schema(description = "Nueva contraseña para el usuario (debe cumplir el patrón)", example = "MiNuevaPassword2@")
    private String newPassword;
}