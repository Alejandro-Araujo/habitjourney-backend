package com.alejandro.habitjourney.backend.common.exception;

/**
 * Excepción lanzada cuando un nombre proporcionado (ej. nombre de usuario)
 * no cumple con los criterios de validación de la aplicación.
 */
public class InvalidNameException extends RuntimeException {

    /**
     * Construye una nueva InvalidNameException con el mensaje especificado.
     *
     * @param message El mensaje detallado de la excepción.
     */
    public InvalidNameException(String message) {super(message);}
}
