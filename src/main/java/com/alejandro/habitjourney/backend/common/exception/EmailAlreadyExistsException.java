package com.alejandro.habitjourney.backend.common.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso (ej. un usuario)
 * con un email que ya existe en el sistema.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Construye una nueva EmailAlreadyExistsException con el mensaje especificado.
     *
     * @param message El mensaje detallado de la excepción.
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}