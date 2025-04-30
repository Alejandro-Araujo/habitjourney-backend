package com.alejandro.habitjourney.backend.common.security;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Implementación de {@link AccessDeniedHandler} para manejar errores de acceso denegado.
 * Se activa cuando un usuario autenticado intenta acceder a un recurso para el que no tiene permisos.
 * Devuelve una respuesta 403 Forbidden con un cuerpo {@link ErrorResponse}.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper;

    /**
     * Constructor que recibe el ObjectMapper configurado por Spring.
     * @param objectMapper El ObjectMapper configurado con módulos JSR310.
     */
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Es llamado cuando un usuario autenticado intenta acceder a un recurso
     * para el que no tiene los permisos necesarios.
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @param accessDeniedException La excepción de acceso denegado.
     * @throws IOException Si ocurre un error de I/O.
     * @throws ServletException Si ocurre un error de servlet.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.warn("Usuario '{}' intentó acceder al recurso protegido: {}",
                    auth.getName(), request.getRequestURI());
        } else {
            logger.warn("Acceso denegado a recurso protegido para usuario no autenticado: {}", request.getRequestURI());
        }

        // Construir el cuerpo de la respuesta de error
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ErrorMessages.TITLE_FORBIDDEN,
                ErrorMessages.MESSAGE_ACCESS_DENIED,
                LocalDateTime.now()
        );

        // Configurar la respuesta HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Escribir el cuerpo de la respuesta en formato JSON
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}