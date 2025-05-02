package com.alejandro.habitjourney.backend.common.exception;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.dto.ErrorResponse;
import com.alejandro.habitjourney.backend.common.dto.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para la API.
 * Captura excepciones lanzadas por los controladores y servicios y las mapea
 * a respuestas HTTP estandarizadas utilizando los DTOs de respuesta de error.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja la excepción {@link EmailAlreadyExistsException} mapeándola a una respuesta 409 Conflict.
     *
     * @param ex La excepción EmailAlreadyExistsException lanzada.
     * @return ResponseEntity con estado 409 y cuerpo ErrorResponse.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        log.warn("Manejo de handleEmailAlreadyExistsException:" + ex.getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.CONFLICT, ErrorMessages.TITLE_CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja la excepción {@link InvalidPasswordException} mapeándola a una respuesta 400 Bad Request.
     * (Asumimos que esta excepción se lanza para problemas de formato o requisitos de contraseña).
     *
     * @param ex La excepción InvalidPasswordException lanzada.
     * @return ResponseEntity con estado 400 y cuerpo ErrorResponse.
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        log.warn("Manejo de handleInvalidPasswordException:" + ex.getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorMessages.TITLE_BAD_REQUEST, ErrorMessages.INVALID_PASSWORD);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja la excepción {@link InvalidNameException} mapeándola a una respuesta 400 Bad Request.
     *
     * @param ex La excepción InvalidNameException lanzada.
     * @return ResponseEntity con estado 400 y cuerpo ErrorResponse.
     */
    @ExceptionHandler(InvalidNameException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNameException(InvalidNameException ex) {
        log.warn("Manejo de handleInvalidNameException:" + ex.getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorMessages.TITLE_BAD_REQUEST, ErrorMessages.INVALID_NAME);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja la excepción {@link UserNotFoundException} mapeándola a una respuesta 404 Not Found.
     *
     * @param ex La excepción UserNotFoundException lanzada.
     * @return ResponseEntity con estado 404 y cuerpo ErrorResponse.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("Manejo de handleUserNotFoundException:" + ex.getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, ErrorMessages.TITLE_RESOURCE_NOT_FOUND, ErrorMessages.USER_NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja la excepción {@link InvalidEmailFormatException} mapeándola a una respuesta 400 Bad Request.
     *
     * @param ex La excepción InvalidEmailFormatException lanzada.
     * @return ResponseEntity con estado 400 y cuerpo ErrorResponse.
     */
    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailFormatException(InvalidEmailFormatException ex) {
        log.warn("Manejo de handleInvalidEmailFormatException:" + ex.getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorMessages.TITLE_BAD_REQUEST, ErrorMessages.VALIDATION_EMAIL_FORMAT);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja la excepción {@link BadCredentialsException} (lanzada típicamente por Spring Security en fallos de login)
     * mapeándola a una respuesta 401 Unauthorized.
     *
     * @param ex La excepción BadCredentialsException lanzada.
     * @return ResponseEntity con estado 401 y cuerpo ErrorResponse.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Manejo de handleBadCredentialsException:" + ex.getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED, ErrorMessages.TITLE_UNAUTHORIZED, ErrorMessages.INVALID_CREDENTIALS);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja la excepción {@link MethodArgumentNotValidException} (lanzada por @Valid)
     * mapeándola a una respuesta 400 Bad Request con detalles de validación.
     *
     * @param ex La excepción MethodArgumentNotValidException lanzada.
     * @return ResponseEntity con estado 400 y cuerpo ValidationErrorResponse.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Manejo de handleValidationExceptions:" + ex.getMessage());
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorMessages.TITLE_VALIDATION_ERROR,
                ErrorMessages.VALIDATION_FAILED,
                LocalDateTime.now()
        );

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja cualquier otra excepción no manejada explícitamente, mapeándola a una respuesta 500 Internal Server Error.
     *
     * @param ex La excepción genérica lanzada.
     * @return ResponseEntity con estado 500 y cuerpo ErrorResponse.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.warn("Manejo de handleGenericException:" + ex.getMessage());
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.TITLE_INTERNAL_SERVER_ERROR, ErrorMessages.GENERIC_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Método auxiliar para construir objetos {@link ErrorResponse}.
     *
     * @param status Estado HTTP del error.
     * @param title Título del error.
     * @param detail Descripción detallada del error.
     * @return Objeto ErrorResponse.
     */
    private ErrorResponse buildErrorResponse(HttpStatus status, String title, String detail) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                title,
                detail,
                LocalDateTime.now()
        );
        return errorResponse;
    }
}