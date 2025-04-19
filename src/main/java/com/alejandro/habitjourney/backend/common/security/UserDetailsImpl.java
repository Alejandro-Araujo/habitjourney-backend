package com.alejandro.habitjourney.backend.common.security;

import com.alejandro.habitjourney.backend.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    private Collection<GrantedAuthority> authorities;

    public static UserDetailsImpl buildUserDetails(User user) {

        // Se asigna un rol básico de USER por defecto
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPasswordHash(), grantedAuthorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
