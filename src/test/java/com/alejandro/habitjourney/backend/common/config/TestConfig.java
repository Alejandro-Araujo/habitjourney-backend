package com.alejandro.habitjourney.backend.common.config;

import com.alejandro.habitjourney.backend.common.exception.GlobalExceptionHandler;
import com.alejandro.habitjourney.backend.common.security.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

/**
 * Clase base con configuraciones y métodos auxiliares para tests
 */
public abstract class TestConfig {

    /**
     * Crea una autenticación mock para tests
     */
    protected Authentication createTestAuthentication(String email) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, email, "password", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}