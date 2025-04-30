package com.alejandro.habitjourney.backend.user.repository;

import com.alejandro.habitjourney.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link User}.
 * Proporciona métodos estándar para operaciones CRUD
 * y métodos de consulta personalizados para acceder a los datos de usuario.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca una entidad User por su correo electrónico.
     *
     * @param email El correo electrónico a buscar.
     * @return Un {@link java.util.Optional} que contiene la entidad User si se encuentra, o vacío si no.
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el correo electrónico especificado.
     *
     * @param email El correo electrónico a verificar.
     * @return true si existe un usuario con ese email, false en caso contrario.
     */
    boolean existsByEmail(String email);
}
