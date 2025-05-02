package com.alejandro.habitjourney.backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * DTO que representa una respuesta de error de la API específica para fallos de validación de entrada.
 * Extiende {@link ErrorResponse} y añade una lista de errores de validación por campo.
 */
@Schema(description = "Respuesta de error específica para fallos de validación de entrada.")
public class ValidationErrorResponse extends ErrorResponse {

    @Schema(description = "Lista de errores de validación por campo.")
    private final List<ValidationError> validationErrors = new ArrayList<>();

    public ValidationErrorResponse(int status, String title, String detail, LocalDateTime timestamp) {
        super(status, title, detail, timestamp);
    }

    public void addValidationError(String field, String message) {
        validationErrors.add(new ValidationError(field, message));
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
