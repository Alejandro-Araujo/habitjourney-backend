package com.alejandro.habitjourney.backend.common.exception;

/**
 * Excepción lanzada cuando un formato de email proporcionado no es válido
 * según las reglas de validación de la aplicación.
 */
public class InvalidEmailFormatException extends RuntimeException {

    /**
     * Construye una nueva InvalidEmailFormatException con el mensaje especificado.
     *
     * @param message El mensaje detallado de la excepción.
     */
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}