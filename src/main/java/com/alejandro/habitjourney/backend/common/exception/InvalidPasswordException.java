package com.alejandro.habitjourney.backend.common.exception;

/**
 * Excepción lanzada cuando una contraseña proporcionada no cumple
 * con los criterios de formato o complejidad de la aplicación.
 */
public class InvalidPasswordException extends RuntimeException {

    /**
     * Construye una nueva InvalidPasswordException con el mensaje especificado.
     *
     * @param message El mensaje detallado de la excepción.
     */
    public InvalidPasswordException(String message) {super(message);}
}
