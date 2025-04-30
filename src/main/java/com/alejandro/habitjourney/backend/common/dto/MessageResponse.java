package com.alejandro.habitjourney.backend.common.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una respuesta simple de la API conteniendo solo un mensaje.
 * Se utiliza para operaciones exitosas que no devuelven datos específicos, como eliminaciones.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta simple de la API conteniendo solo un mensaje.")
public class MessageResponse {
    @Schema(description = "Mensaje descriptivo del resultado de la operación.", example = "Operación completada con éxito")
    private String message;
}