package com.alejandro.habitjourney.backend.common.security;


import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Implementación de {@link AuthenticationEntryPoint} para manejar errores de autenticación.
 * Se activa cuando un usuario no autenticado intenta acceder a un recurso protegido.
 * Devuelve una respuesta 401 Unauthorized con un cuerpo {@link ErrorResponse}.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    private final ObjectMapper objectMapper;

    /**
     * Constructor que recibe el ObjectMapper configurado por Spring.
     * @param objectMapper El ObjectMapper configurado con módulos JSR310.
     */
    public AuthEntryPointJwt(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Es llamado para comenzar un esquema de autenticación.
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @param authException La excepción de autenticación que causó el fallo.
     * @throws IOException Si ocurre un error de I/O.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        logger.error("Error de autenticación no autorizado: {}", authException.getMessage());

        // Construir el cuerpo de la respuesta de error
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorMessages.TITLE_UNAUTHORIZED,
                ErrorMessages.MESSAGE_AUTHENTICATION_REQUIRED,
                LocalDateTime.now()
        );

        // Configurar la respuesta HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Escribir el cuerpo de la respuesta en formato JSON
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
