package com.alejandro.habitjourney.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa la estructura de respuesta estándar
 * para las operaciones exitosas relacionadas con la información del usuario.
 * Incluye un mensaje de estado y los datos del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estructura de respuesta estándar para operaciones exitosas con datos de usuario.")
public class UserResponseDTO {

    @Schema(description = "Mensaje descriptivo del resultado de la operación.", example = "Usuario encontrado")
    private String message;

    @Schema(description = "Datos del usuario.", implementation = UserDTO.class)
    private UserDTO user;
}
