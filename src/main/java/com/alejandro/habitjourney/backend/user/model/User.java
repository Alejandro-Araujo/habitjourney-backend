package com.alejandro.habitjourney.backend.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un usuario en el sistema.
 * Mapea a la tabla "users" en la base de datos y contiene la información principal del usuario.
 */
@Entity
@Table(name= "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash",nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
