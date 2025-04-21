    package com.alejandro.habitjourney.backend.user.controller;

    import com.alejandro.habitjourney.backend.common.config.SecurityTestConfig;
    import com.alejandro.habitjourney.backend.common.exception.*;
    import com.alejandro.habitjourney.backend.common.security.UserDetailsImpl;
    import com.alejandro.habitjourney.backend.user.dto.PasswordChangeDTO;
    import com.alejandro.habitjourney.backend.user.dto.UserDTO;
    import com.alejandro.habitjourney.backend.user.service.UserService;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.http.MediaType;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.test.context.ContextConfiguration;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;

    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

    import java.util.Collections;
    import java.util.List;

    import static org.mockito.ArgumentMatchers.*;
    import static org.mockito.Mockito.*;
    import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

    @WebMvcTest
    @ContextConfiguration(classes = {SecurityTestConfig.class, UserControllerTestConfig.class})
    class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;


        @MockitoBean
        private UserService userService;

        private UserDTO testUserDTO;
        private PasswordChangeDTO validPasswordChangeDTO;

        @BeforeEach
        void setUp() {
            testUserDTO = new UserDTO(1L, "Test User", "test@example.com");
            validPasswordChangeDTO = new PasswordChangeDTO("currentPassword", "newValidPassword123!");

        }

        // Método auxiliar para crear la autenticación
        private Authentication createTestAuthentication(String email) {
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            UserDetailsImpl userDetails = new UserDetailsImpl(1L, email, "password", authorities);
            return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        }

        @Test
        void getCurrentUser_ShouldReturnUserData() throws Exception {
            when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);

            mockMvc.perform(get("/api/users/me")
                            .with(authentication(createTestAuthentication("test@example.com"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Usuario encontrado"))
                    .andExpect(jsonPath("$.user.id").value(1))
                    .andExpect(jsonPath("$.user.name").value("Test User"))
                    .andExpect(jsonPath("$.user.email").value("test@example.com"));
        }

        @Test
        void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
            UserDTO updatedUser = new UserDTO(1L, "Updated Name", "test@example.com");

            when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
            when(userService.updateUser(anyLong(), any(UserDTO.class))).thenReturn(updatedUser);

            mockMvc.perform(put("/api/users/me")
                            .with(authentication(createTestAuthentication("test@example.com")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Usuario actualizado correctamente"))
                    .andExpect(jsonPath("$.user.name").value("Updated Name"));
        }

        @Test
        void deleteUser_ShouldReturnNoContent() throws Exception {
            when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
            doNothing().when(userService).deleteUser(anyLong());

            mockMvc.perform(delete("/api/users/me")
                            .with(authentication(createTestAuthentication("test@example.com"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Cuenta eliminada correctamente"));
        }

        @Test
        void changePassword_WithValidData_ShouldReturnNoContent() throws Exception {
            when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
            doNothing().when(userService).changePassword(anyLong(), anyString(), anyString());

            mockMvc.perform(post("/api/users/me/change-password")
                            .with(authentication(createTestAuthentication("test@example.com")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente"));
        }

        @Test
        void updateUser_WithEmailAlreadyExists_ShouldReturnBadRequest() throws Exception {
            when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
            when(userService.updateUser(anyLong(), any(UserDTO.class)))
                    .thenThrow(new EmailAlreadyExistsException("Ya existe un usuario con este correo electrónico"));

            mockMvc.perform(put("/api/users/me")
                            .with(authentication(createTestAuthentication("test@example.com")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400)) // Añade esta aserción
                    .andExpect(jsonPath("$.title").value("Registro inválido"))
                    .andExpect(jsonPath("$.detail").value("Ya existe un usuario con este correo electrónico"))
                    .andExpect(jsonPath("$.timestamp").exists()); // Asegúrate de que esperas el timestamp
        }

        @Test
        void changePassword_WithIncorrectCurrentPassword_ShouldReturnBadRequest() throws Exception {
            when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
            doThrow(new InvalidCredentialsException("La contraseña actual es incorrecta"))
                    .when(userService).changePassword(anyLong(), anyString(), anyString());

            mockMvc.perform(post("/api/users/me/change-password")
                            .with(authentication(createTestAuthentication("test@example.com")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Credenciales inválidas"));
        }

        @Test
        void changePassword_WithInvalidNewPassword_ShouldReturnBadRequest() throws Exception {
            when(userService.getUserByEmail(anyString())).thenReturn(testUserDTO);
            doThrow(new InvalidPasswordException("La contraseña debe tener entre 6 y 32 caracteres"))
                    .when(userService).changePassword(anyLong(), anyString(), anyString());

            mockMvc.perform(post("/api/users/me/change-password")
                            .with(authentication(createTestAuthentication("test@example.com")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPasswordChangeDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Contraseña inválida"))
                    .andExpect(jsonPath("$.detail").value("La contraseña debe tener entre 6 y 32 caracteres"));
        }

        @Test
        void getUserData_WithUserNotFound_ShouldReturnNotFound() throws Exception {
            when(userService.getUserByEmail(anyString()))
                    .thenThrow(new UserNotFoundException("Usuario no encontrado"));

            mockMvc.perform(get("/api/users/me")
                            .with(authentication(createTestAuthentication("nonexistent@example.com"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Usuario no encontrado"))
                    .andExpect(jsonPath("$.detail").value("Usuario no encontrado"));
        }
    }