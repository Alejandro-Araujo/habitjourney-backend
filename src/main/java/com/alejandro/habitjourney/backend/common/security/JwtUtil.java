package com.alejandro.habitjourney.backend.common.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.parser = Jwts.parser().verifyWith(signingKey).build();
        logger.info("Signing Key inicializada correctamente");
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationInMs);

        List<String> roles = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder()
                .subject(userDetails.getEmail())
                .claim("id", userDetails.getId())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = parser.parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            logger.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        try {
            Jws<Claims> claimsJws = parser.parseSignedClaims(token);
            logger.debug("Claims extraídos del token: {}", claimsJws.getPayload());
            return claimsJws.getPayload();
        } catch (JwtException e) {
            logger.error("Error al extraer claims del token: {}", e.getMessage());
            throw e;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    public Authentication getAuthentication(String token) {
        String email = extractSubject(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
