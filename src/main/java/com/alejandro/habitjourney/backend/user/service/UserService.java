package com.alejandro.habitjourney.backend.user.service;

import com.alejandro.habitjourney.backend.common.exception.EmailAlreadyExistsException;
import com.alejandro.habitjourney.backend.common.exception.InvalidCredentialsException;
import com.alejandro.habitjourney.backend.common.exception.InvalidPasswordException;
import com.alejandro.habitjourney.backend.common.exception.UserNotFoundException;
import com.alejandro.habitjourney.backend.common.util.ValidationUtils;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.mapper.UserMapper;
import com.alejandro.habitjourney.backend.user.model.User;
import com.alejandro.habitjourney.backend.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return userMapper.userToUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser (Long id, UserDTO updateUserDTO) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!user.getEmail().equals(updateUserDTO.getEmail()) &&
                userRepository.existsByEmail(updateUserDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con este correo electrónico");
        }

        user.setName(updateUserDTO.getName());
        user.setEmail(updateUserDTO.getEmail());
        user = userRepository.save(user);
        return userMapper.userToUserDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("La contraseña actual es incorrecta");
        }

        String passwordValidation = ValidationUtils.validatePassword(newPassword);
        if (passwordValidation != null) {
            throw new InvalidPasswordException(passwordValidation);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return userMapper.userToUserDTO(user);
    }

}
