package com.alejandro.habitjourney.backend.common.constant;

/**
 * Clase que contiene constantes para los mensajes de éxito utilizados en las respuestas de la API.
 * Centraliza los mensajes para facilitar la gestión y la coherencia.
 */
public class SuccessMessages {

    public static final String USER_REGISTERED_SUCCESS = "Usuario registrado con éxito";
    public static final String LOGIN_SUCCESS = "Login exitoso";
    public static final String USER_FOUND_SUCCESS = "Usuario encontrado";
    public static final String USER_UPDATED_SUCCESS = "Usuario actualizado correctamente";
    public static final String USER_DELETED_SUCCESS = "Cuenta eliminada correctamente";
    public static final String PASSWORD_CHANGED_SUCCESS = "Contraseña actualizada correctamente";
    public static final String AUTHENTICATION_SUCCESS = "Petición autenticada con token JWT";

    public static final String JWT_INIT_SUCCESS = "Signing Key y Parser JWT inicializados correctamente";
    public static final String JWT_TOKEN_VALIDATED = "Token JWT validado correctamente.";
    public static final String JWT_CLAIMS_EXTRACTED = "Claims extraídos del token: {}";
    public static final String JWT_BEARER_FOUND = "Token 'Bearer' encontrado en la cabecera Authorization.";

    // Mensaje genérico para operaciones exitosas sin datos de retorno específicos
    public static final String OPERATION_SUCCESS = "Operación completada con éxito";

}
