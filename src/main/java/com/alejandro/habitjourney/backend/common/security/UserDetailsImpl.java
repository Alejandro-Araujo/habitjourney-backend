package com.alejandro.habitjourney.backend.common.security;

import com.alejandro.habitjourney.backend.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Implementación de {@link UserDetails} de Spring Security.
 * Representa al usuario autenticado en el contexto de seguridad,
 * conteniendo información básica como ID, email, contraseña hasheada y roles/autoridades.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Detalles del usuario principal para Spring Security")
public class UserDetailsImpl implements UserDetails {

    @Schema(description = "ID único del usuario", example = "123")
    private Long id;

    @Schema(description = "Email del usuario (nombre de usuario)", example = "usuario@example.com")
    private String email;

    @JsonIgnore
    @Schema(description = "Contraseña hasheada del usuario (no expuesta en la API)")
    private String password;

    @Schema(description = "Autoridades/Roles del usuario")
    private Collection<GrantedAuthority> authorities;

    /**
     * Método factory estático para construir una instancia de UserDetailsImpl
     * a partir de un objeto {@link User} del modelo.
     * Asigna un rol básico por defecto.
     *
     * @param user El objeto User del modelo.
     * @return Una nueva instancia de UserDetailsImpl.
     */
    public static UserDetailsImpl buildUserDetails(User user) {

        // Se asigna un rol básico de USER por defecto
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPasswordHash(), grantedAuthorities);
    }

    // Métodos de la interfaz UserDetails

    /**
     * Retorna las autoridades otorgadas al usuario.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Retorna las autoridades otorgadas al usuario.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Retorna el nombre de usuario utilizado para autenticar al usuario.
     * En este caso, es el email del usuario.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica si la cuenta del usuario ha expirado.
     *
     * @return true si la cuenta es válida (no expirada), false en caso contrario.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario está bloqueado o desbloqueado.
     *
     * @return true si el usuario no está bloqueado, false en caso contrario.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica si las credenciales (contraseña) del usuario han expirado.
     *
     * @return true si las credenciales son válidas (no expiradas), false en caso contrario.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario está habilitado o deshabilitado.
     *
     * @return true si el usuario está habilitado, false en caso contrario.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
