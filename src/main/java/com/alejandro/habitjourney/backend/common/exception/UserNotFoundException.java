package com.alejandro.habitjourney.backend.common.exception;

/**
 * Excepción lanzada cuando un usuario no puede ser encontrado
 * basándose en los criterios proporcionados (ej. ID, email).
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Construye una nueva UserNotFoundException con el mensaje especificado.
     *
     * @param message El mensaje detallado de la excepción.
     */
    public UserNotFoundException(String message) { super(message); }
}
