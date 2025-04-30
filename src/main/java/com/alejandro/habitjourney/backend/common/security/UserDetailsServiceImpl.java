package com.alejandro.habitjourney.backend.common.security;


import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.exception.UserNotFoundException;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link UserDetailsService} de Spring Security.
 * Carga los detalles del usuario desde la base de datos utilizando el email como identificador.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    /**
     * Carga los detalles del usuario por su nombre de usuario (email).
     * Este método es llamado por el proveedor de autenticación de Spring Security.
     *
     * @param email El email del usuario.
     * @return Una implementación de UserDetails que contiene los detalles del usuario.
     * @throws UserNotFoundException Si no se encuentra un usuario con el email proporcionado.
     */
    @Override
    public UserDetailsImpl loadUserByUsername(String email) throws UserNotFoundException {
        logger.debug("Intentando cargar usuario por email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorMessages.USER_NOT_FOUND));

        UserDetailsImpl userDetails = UserDetailsImpl.buildUserDetails(user);
        logger.debug("Usuario encontrado y UserDetails construido para email: {}", email);
        return userDetails;
    }
}
