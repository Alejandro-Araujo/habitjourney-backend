package com.alejandro.habitjourney.backend.common.security;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.constant.SuccessMessages;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro personalizado para la autenticación basada en JSON Web Tokens (JWT).
 * Se ejecuta una vez por cada petición.
 * Busca un token JWT en la cabecera Authorization, lo valida
 * y configura el contexto de seguridad si es válido.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") || path.equals("/error");
    }

    /**
     * Realiza el filtrado interno de la petición.
     * Busca, valida y procesa el token JWT.
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @param filterChain La cadena de filtros.
     * @throws ServletException Si ocurre un error de servlet.
     * @throws IOException Si ocurre un error de I/O.
     * @throws AuthenticationServiceException Si hay un error al procesar o validar el token JWT.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = jwtUtil.resolveToken(request);
            if (token != null) {
                if (jwtUtil.validateToken(token)) {
                    Authentication authentication = jwtUtil.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug(SuccessMessages.AUTHENTICATION_SUCCESS);
                }
            }
        }catch (JwtException | AuthenticationException e) {
                    SecurityContextHolder.clearContext();
                    logger.error("JWT error: {}", e.getMessage());
                    throw new AuthenticationServiceException(ErrorMessages.AUTHENTICATION_ERROR, e);
                }

        filterChain.doFilter(request, response);
    }
}
