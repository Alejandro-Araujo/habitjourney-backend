package com.alejandro.habitjourney.backend.common.util;


import com.alejandro.habitjourney.backend.auth.dto.LoginRequestDTO;
import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.user.dto.PasswordChangeDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.model.User;

/**
 * Factory para crear objetos de prueba reutilizables
 */
public class TestDataFactory {

    // User objects
    public static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        return user;
    }

    public static UserDTO createTestUserDTO() {
        return new UserDTO(1L, "Test User", "test@example.com");
    }

    // Auth objects
    public static RegisterRequestDTO createValidRegisterRequest() {
        return new RegisterRequestDTO("Test User", "test@example.com", "Valid1Password!");
    }

    public static RegisterRequestDTO createInvalidRegisterRequest() {
        return new RegisterRequestDTO("", "invalid-email", "weak");
    }

    public static LoginRequestDTO createValidLoginRequest() {
        return new LoginRequestDTO("test@example.com", "Valid1Password!");
    }

    public static LoginRequestDTO createInvalidLoginRequest() {
        return new LoginRequestDTO("bad-email", "");
    }

    // Password change
    public static PasswordChangeDTO createValidPasswordChangeDTO() {
        return new PasswordChangeDTO("currentPassword", "newValidPassword123!");
    }

    // Variations for specific tests
    public static User createUserWithId(Long id) {
        User user = createTestUser();
        user.setId(id);
        return user;
    }

    public static UserDTO createUpdatedUserDTO() {
        return new UserDTO(1L, "Updated Name", "test@example.com");
    }
}