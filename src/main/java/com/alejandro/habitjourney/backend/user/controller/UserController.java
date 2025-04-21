package com.alejandro.habitjourney.backend.user.controller;


import com.alejandro.habitjourney.backend.common.dto.MessageResponse;
import com.alejandro.habitjourney.backend.common.security.UserDetailsImpl;
import com.alejandro.habitjourney.backend.user.dto.PasswordChangeDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.dto.UserResponseDTO;
import com.alejandro.habitjourney.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String email = userDetails.getUsername();
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok( new UserResponseDTO("Usuario encontrado",user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @Valid @RequestBody UserDTO userDTO) {
        String email = userDetails.getUsername();
        UserDTO currentUser = userService.getUserByEmail(email);
        UserDTO updatedUser = userService.updateUser(currentUser.getId(), userDTO);
        return ResponseEntity.ok(new UserResponseDTO("Usuario actualizado correctamente",updatedUser));
    }

    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String email = userDetails.getUsername();
        UserDTO currentUser = userService.getUserByEmail(email);
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Cuenta eliminada correctamente"));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        String email = userDetails.getUsername();
        UserDTO currentUser = userService.getUserByEmail(email);
        userService.changePassword(currentUser.getId(), passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada correctamente"));
    }
}
