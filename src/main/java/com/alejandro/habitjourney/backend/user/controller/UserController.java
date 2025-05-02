package com.alejandro.habitjourney.backend.user.controller;


import com.alejandro.habitjourney.backend.auth.service.AuthService;
import com.alejandro.habitjourney.backend.common.constant.SuccessMessages;
import com.alejandro.habitjourney.backend.common.dto.MessageResponse;
import com.alejandro.habitjourney.backend.common.security.UserDetailsImpl;
import com.alejandro.habitjourney.backend.user.dto.PasswordChangeDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.dto.UserResponseDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.service.UserService;
import com.alejandro.habitjourney.backend.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que gestiona las operaciones relacionadas con los usuarios.
 * Proporciona endpoints para consultar, actualizar y eliminar la información del usuario actual,
 * así como para cambiar la contraseña.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "Usuario", description = "API para gestionar operaciones del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthService authService;

    /**
     * Obtiene la información del usuario actualmente autenticado.
     *
     * @param userDetails Detalles del usuario autenticado
     * @return Respuesta con los datos del usuario actual
     */
    @GetMapping("/me")
    @Operation(summary = "Obtener información del usuario actual",
            description = "Devuelve los datos del usuario autenticado en la sesión actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información del usuario recuperada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado, se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.debug("Solicitud para obtener información del usuario actual: {}", userDetails.getUsername());
        User user = authService.getAuthenticatedUser();
        UserDTO userDTO = userMapper.userToUserDTO(user);
        log.info("Información del usuario {} recuperada correctamente", userDetails.getUsername());
        return ResponseEntity.ok( new UserResponseDTO(SuccessMessages.USER_FOUND_SUCCESS,userDTO));
    }

    /**
     * Actualiza los datos del usuario actualmente autenticado.
     *
     * @param userDetails Detalles del usuario autenticado
     * @param userDTO Datos actualizados del usuario
     * @return Respuesta con los datos actualizados del usuario
     */
    @PutMapping("/me")
    @Operation(summary = "Actualizar información del usuario actual",
            description = "Actualiza los datos del usuario autenticado en la sesión actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de usuario inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado, se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El email ya está en uso por otro usuario")
    })
    public ResponseEntity<UserResponseDTO> updateUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @Valid @RequestBody UserDTO userDTO) {
        log.info("Solicitud para actualizar usuario: {} con datos: {}", userDetails.getUsername(), userDTO);
        User user = authService.getAuthenticatedUser();
        Long userId = user.getId();
        User updatedUserEntity = userService.updateUser(userId, userDTO);
        UserDTO updatedUserDTO = userMapper.userToUserDTO(updatedUserEntity);
        log.info("Usuario {} actualizado correctamente", userDetails.getUsername());
        return ResponseEntity.ok(new UserResponseDTO(SuccessMessages.USER_UPDATED_SUCCESS,updatedUserDTO));
    }

    /**
     * Elimina la cuenta del usuario actualmente autenticado.
     *
     * @param userDetails Detalles del usuario autenticado
     * @return Respuesta con mensaje de confirmación
     */
    @DeleteMapping("/me")
    @Operation(summary = "Eliminar cuenta del usuario actual",
            description = "Elimina la cuenta del usuario autenticado en la sesión actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta eliminada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado, se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<MessageResponse> deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Solicitud para eliminar la cuenta del usuario: {}", userDetails.getUsername());
        User user = authService.getAuthenticatedUser();
        Long userId = user.getId();
        userService.deleteUser(userId);
        log.info("Cuenta del usuario {} eliminada correctamente", userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse(SuccessMessages.USER_DELETED_SUCCESS));
    }

    /**
     * Cambia la contraseña del usuario actualmente autenticado.
     *
     * @param userDetails Detalles del usuario autenticado
     * @param passwordChangeDTO DTO con la contraseña actual y nueva
     * @return Respuesta con mensaje de confirmación
     */
    @PostMapping("/me/change-password")
    @Operation(summary = "Cambiar contraseña del usuario actual",
            description = "Actualiza la contraseña del usuario autenticado en la sesión actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de contraseña inválidos o no cumple con los requisitos"),
            @ApiResponse(responseCode = "401", description = "No autorizado o contraseña actual incorrecta"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        log.info("Solicitud para cambiar contraseña del usuario: {}", userDetails.getUsername());
        User user = authService.getAuthenticatedUser();
        Long userId = user.getId();
        userService.changePassword(userId, passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
        log.info("Contraseña del usuario {} actualizada correctamente", userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse(SuccessMessages.PASSWORD_CHANGED_SUCCESS));
    }
}
