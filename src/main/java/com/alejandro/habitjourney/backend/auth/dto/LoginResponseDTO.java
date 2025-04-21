package com.alejandro.habitjourney.backend.auth.dto;


import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de autenticación de usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String message;
    private String token;
    private UserDTO user;
}