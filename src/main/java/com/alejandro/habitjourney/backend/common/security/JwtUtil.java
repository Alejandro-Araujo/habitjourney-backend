package com.alejandro.habitjourney.backend.common.security;


import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.constant.SuccessMessages;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase de utilidad para generar, validar y extraer información de JSON Web Tokens (JWT).
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final UserDetailsServiceImpl userDetailsService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private SecretKey signingKey;

    private JwtParser parser;

    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Método de inicialización que se ejecuta después de que el Bean JwtUtil ha sido creado.
     * Decodifica la clave secreta Base64 y configura el parser JWT.
     */
    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            this.parser = Jwts.parser().verifyWith(signingKey).build();
            logger.info(SuccessMessages.JWT_INIT_SUCCESS);
        } catch (IllegalArgumentException e) {
            logger.error(ErrorMessages.JWT_SECRET_DECODE_ERROR, e);
            throw new RuntimeException(ErrorMessages.JWT_SECRET_INVALID, e);
        } catch (Exception e) {
            logger.error(ErrorMessages.JWT_INIT_UNEXPECTED_ERROR, e);
            throw new RuntimeException(ErrorMessages.JWT_INIT_ERROR, e);
        }
    }

    /**
     * Genera un token de acceso JWT para un usuario autenticado.
     * El token incluye el email del usuario como subject, su ID y roles como claims.
     *
     * @param authentication El objeto Authentication que representa al usuario autenticado.
     * @return El token JWT generado.
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationInMs);

        List<String> roles = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = Jwts.builder()
                .subject(userDetails.getEmail())
                .claim("id", userDetails.getId())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        logger.debug("Token JWT generado para el usuario: {}", userDetails.getEmail());
        return token;
    }

    /**
     * Valida la firma y la expiración de un token JWT.
     *
     * @param token El token JWT a validar.
     * @return true si el token es válido (firma correcta, no expirado), false en caso contrario.
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.debug("Intento de validar token JWT nulo o vacío.");
            return false;
        }
        try {
            parser.parseSignedClaims(token);
            logger.debug(SuccessMessages.JWT_TOKEN_VALIDATED);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Validación de token fallida: token expirado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Validación de token fallida: token malformado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("Validación de token fallida: token no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Validación de token fallida: argumento ilegal: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("Validación de token fallida: firma inválida: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Extrae el subject (normalmente el email del usuario) de un token JWT.
     *
     * @param token El token JWT del que extraer el subject.
     * @return El subject del token.
     * @throws JwtException Si el token es inválido o no se pueden extraer los claims.
     */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrae todos los claims (payload) de un token JWT.
     *
     * @param token El token JWT del que extraer los claims.
     * @return Los Claims del token.
     * @throws JwtException Si el token es inválido o no se pueden extraer los claims.
     */
    public Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn(ErrorMessages.JWT_TOKEN_NULL_OR_EMPTY_EXTRACT_CLAIMS);
            throw new IllegalArgumentException(ErrorMessages.JWT_TOKEN_NULL_OR_EMPTY);
        }
        try {
            Jws<Claims> claimsJws = parser.parseSignedClaims(token);
            logger.debug(SuccessMessages.JWT_CLAIMS_EXTRACTED, claimsJws.getPayload());
            return claimsJws.getPayload();
        } catch (JwtException e) {
            logger.error(ErrorMessages.JWT_CLAIMS_EXTRACTION_ERROR, e.getMessage());
            throw e;
        }
    }

    /**
     * Resuelve y extrae el token JWT de la cabecera Authorization de una petición HTTP.
     * Espera el formato "Bearer TOKEN".
     *
     * @param request La petición HTTP.
     * @return El token JWT (sin el prefijo "Bearer ") o null si no se encuentra o el formato es incorrecto.
     */
    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            logger.debug(SuccessMessages.JWT_BEARER_FOUND);
            return header.substring(7);
        }
        logger.debug(ErrorMessages.JWT_BEARER_NOT_FOUND);
        return null;
    }

    /**
     * Crea un objeto Authentication a partir de un token JWT válido.
     * Carga los detalles completos del usuario usando el UserDetailsService.
     *
     * @param token El token JWT válido.
     * @return Un objeto Authentication representando al usuario autenticado.
     */
    public Authentication getAuthentication(String token) {
        String email = extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        logger.debug(SuccessMessages.AUTHENTICATION_SUCCESS);
        return authenticationToken;
    }
}
