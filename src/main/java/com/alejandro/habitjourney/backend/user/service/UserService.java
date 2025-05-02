package com.alejandro.habitjourney.backend.user.service;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import com.alejandro.habitjourney.backend.common.constant.SuccessMessages;
import com.alejandro.habitjourney.backend.common.exception.EmailAlreadyExistsException;
import com.alejandro.habitjourney.backend.common.exception.InvalidPasswordException;
import com.alejandro.habitjourney.backend.common.exception.UserNotFoundException;
import com.alejandro.habitjourney.backend.common.util.ValidationUtils;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que gestiona la lógica de negocio relacionada con las operaciones de usuario.
 * Se encarga de la interacción con el repositorio de usuarios, validaciones de negocio,
 * codificación de contraseñas y coordinación de las operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * y cambio de contraseña para las entidades {@link User}.
 * Este servicio trabaja con entidades de dominio y lanza excepciones de negocio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios existentes.
     *
     * @return Lista de entidades User.
     */
    public List<User> getAllUsers() {
        log.debug("Obteniendo todos los usuarios (entidades)");
        return userRepository.findAll();
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Entidad User encontrada.
     * @throws UserNotFoundException si el usuario no existe.
     */
    public User getUserById(Long id) {
        log.debug("Obteniendo usuario por ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(()  -> {
                    log.warn("Usuario no encontrado con ID: {}", id);
                    return new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
                });
        log.info(SuccessMessages.USER_FOUND_SUCCESS + " con ID: {}", id);
        return user;
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id ID del usuario a actualizar.
     * @param updateUserDTO DTO con los nuevos datos del usuario.
     * @return Entidad User actualizada.
     * @throws UserNotFoundException si el usuario no existe.
     * @throws EmailAlreadyExistsException si el nuevo email ya está en uso por otro usuario.
     */
    @Transactional
    public User updateUser (Long id, UserDTO updateUserDTO) {
        log.debug("Actualizando usuario con ID: {} con datos: {}", id, updateUserDTO);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado para actualizar con ID: {}", id);
                    return new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
                });
        if (!user.getEmail().equals(updateUserDTO.getEmail()) &&
                userRepository.existsByEmail(updateUserDTO.getEmail())) {
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_EXISTS);
        }
        user.setName(updateUserDTO.getName());
        user.setEmail(updateUserDTO.getEmail());
        user = userRepository.save(user);
        log.info(SuccessMessages.USER_UPDATED_SUCCESS + " con ID: {}", id);
        return user;
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id ID del usuario a eliminar.
     * @throws UserNotFoundException si el usuario no existe.
     */
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Eliminando usuario con ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Intento de eliminar usuario no encontrado con ID: {}", id);
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
        log.info(SuccessMessages.USER_DELETED_SUCCESS + " con ID: {}", id);
    }

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param userId ID del usuario.
     * @param currentPassword Contraseña actual.
     * @param newPassword Nueva contraseña.
     * @throws UserNotFoundException si el usuario no existe.
     * @throws BadCredentialsException si la contraseña actual es incorrecta.
     * @throws InvalidPasswordException si la nueva contraseña no cumple con los requisitos de formato.
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.debug("Intentando cambiar contraseña para usuario con ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado para cambiar contraseña con ID: {}", userId);
                    return new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadCredentialsException (ErrorMessages.CURRENT_PASSWORD_INCORRECT);
        }

        String passwordValidation = ValidationUtils.validatePassword(newPassword);
        if (passwordValidation != null) {
            throw new InvalidPasswordException(passwordValidation);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info(SuccessMessages.PASSWORD_CHANGED_SUCCESS + " para usuario con ID: {}", userId);
    }

    /**
     * Obtiene un usuario por su email.
     *
     * @param email Email del usuario.
     * @return Entidad User encontrada.
     * @throws UserNotFoundException si el usuario no existe.
     */
    public User getUserByEmail(String email) {
        log.debug("Obteniendo usuario por email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con email: {}", email);
                    return new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
                });
        log.info(SuccessMessages.USER_FOUND_SUCCESS + " con email: {}", email);
        return user;
    }

}
