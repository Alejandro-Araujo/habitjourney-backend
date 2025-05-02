package com.alejandro.habitjourney.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO que representa la estructura estándar de respuesta para errores de la API.
 * Incluye el estado HTTP, un título, un detalle descriptivo y una marca de tiempo.
 */
@Data
@AllArgsConstructor
@Schema(description = "Estructura estándar para respuestas de error de la API.")
public class ErrorResponse {
    @Schema(description = "Código de estado HTTP.", example = "400")
    private int status;

    @Schema(description = "Título corto del error.", example = "Error de validación")
    private String title;

    @Schema(description = "Descripción detallada del error.", example = "Los datos enviados no cumplen los requisitos")
    private String detail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Marca de tiempo del error.", example = "2023-10-27T10:30:00")
    private LocalDateTime timestamp;
}
