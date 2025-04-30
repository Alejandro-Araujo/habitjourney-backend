package com.alejandro.habitjourney.backend.common.constant;

/**
 * Clase que contiene constantes para los mensajes de error utilizados en la aplicación.
 * Centraliza los mensajes para facilitar la gestión y la coherencia.
 */
public class ErrorMessages {

    public static final String VALIDATION_EMAIL_EMPTY = "El email no puede estar vacío";
    public static final String VALIDATION_EMAIL_FORMAT = "El formato del correo electrónico no es válido";
    public static final String VALIDATION_PASSWORD_NULL = "La contraseña no puede ser nula";
    public static final String VALIDATION_PASSWORD_MIN_LENGTH = "La contraseña debe tener al menos %d caracteres";
    public static final String VALIDATION_PASSWORD_MAX_LENGTH = "La contraseña no puede tener más de %d caracteres";
    public static final String VALIDATION_PASSWORD_UPPERCASE = "La contraseña debe contener al menos una mayúscula";
    public static final String VALIDATION_PASSWORD_LOWERCASE = "La contraseña debe contener al menos una minúscula";
    public static final String VALIDATION_PASSWORD_DIGIT = "La contraseña debe contener al menos un dígito";
    public static final String VALIDATION_PASSWORD_SPECIAL_CHAR = "La contraseña debe contener al menos un carácter especial";
    public static final String VALIDATION_NAME_EMPTY = "El nombre no puede estar vacío";
    public static final String VALIDATION_NAME_MIN_LENGTH = "El nombre debe tener al menos %d caracteres";

    public static final String EMAIL_EXISTS = "Ya existe un usuario con este correo electrónico";
    public static final String INVALID_PASSWORD = "La contraseña no cumple los requisitos";
    public static final String INVALID_NAME = "El nombre proporcionado no es válido";
    public static final String INVALID_CREDENTIALS = "Correo o contraseña incorrectos";
    public static final String USER_NOT_FOUND = "Usuario no encontrado";
    public static final String VALIDATION_FAILED = "Los datos enviados no cumplen los requisitos";
    public static final String INVALID_REGISTER = "Registro inválido";
    public static final String GENERIC_ERROR = "Error interno del servidor";
    public static final String CURRENT_PASSWORD_INCORRECT = "La contraseña actual es incorrecta";


    public static final String TITLE_VALIDATION_ERROR = "Error de validación de la solicitud";
    public static final String TITLE_RESOURCE_NOT_FOUND = "Recurso no encontrado";
    public static final String TITLE_CONFLICT = "Conflicto de datos";
    public static final String TITLE_BAD_REQUEST="Petición incorrecta";
    public static final String TITLE_UNAUTHORIZED= "Acceso no autorizado";
    public static final String TITLE_INTERNAL_SERVER_ERROR= "Error interno";
    public static final String TITLE_FORBIDDEN = "Forbidden";

    public static final String MESSAGE_AUTHENTICATION_REQUIRED = "Se requiere autenticación para acceder a este recurso.";
    public static final String MESSAGE_INVALID_TOKEN = "Token de autenticación inválido o ausente.";
    public static final String MESSAGE_ACCESS_DENIED = "No tiene permisos suficientes para acceder a este recurso.";
    public static final String AUTHENTICATION_ERROR = "Error de autenticación";

    public static final String JWT_SECRET_DECODE_ERROR = "Error al decodificar la clave secreta JWT. Asegúrese de que 'jwt.secret' es una cadena Base64 válida.";
    public static final String JWT_SECRET_INVALID = "Error de configuración JWT: Clave secreta no válida.";
    public static final String JWT_INIT_UNEXPECTED_ERROR = "Error inesperado durante la inicialización de JwtUtil";
    public static final String JWT_INIT_ERROR = "Error al inicializar JwtUtil";
    public static final String JWT_TOKEN_NULL_OR_EMPTY_EXTRACT_CLAIMS = "Intento de extraer claims de token JWT nulo o vacío.";
    public static final String JWT_TOKEN_NULL_OR_EMPTY = "El token no puede ser nulo o vacío";
    public static final String JWT_CLAIMS_EXTRACTION_ERROR = "Error al extraer claims del token: {}";
    public static final String JWT_BEARER_NOT_FOUND = "No se encontró token 'Bearer' en la cabecera Authorization.";

}
