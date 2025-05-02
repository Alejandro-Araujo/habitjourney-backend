package com.alejandro.habitjourney.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa la información básica de un usuario.
 * Se utiliza para la comunicación de datos del usuario entre la capa de presentación (controlador)
 * y la capa de servicio, así como en las respuestas de la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que representa la información básica de un usuario para la API.")
public class UserDTO {

    @Schema(description = "Identificador único del usuario.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe contener entre 2 y 50 caracteres")
    @Schema(description = "Nombre completo del usuario.", example = "Jane Doe", required = true, minLength = 2, maxLength = 50)
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Schema(description = "Correo electrónico del usuario (debe ser único).", example = "jane.doe@example.com", required = true, format = "email")
    private String email;
}
