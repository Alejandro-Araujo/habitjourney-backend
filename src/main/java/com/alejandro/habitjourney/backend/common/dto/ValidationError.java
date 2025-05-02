package com.alejandro.habitjourney.backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO que representa un error de validación de un campo específico.
 * Utilizado dentro de {@link ValidationErrorResponse}.
 */
@Data
@AllArgsConstructor
@Schema(description = "Detalle de un error de validación para un campo específico.")
public class ValidationError {

    @Schema(description = "Nombre del campo que falló la validación.", example = "email")
    private String field;

    @Schema(description = "Mensaje de error de validación para el campo.", example = "Debe ser un email válido")
    private String message;
}
