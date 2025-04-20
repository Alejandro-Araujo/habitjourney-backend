package com.alejandro.habitjourney.backend.user.mapper;


import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true) // La contraseña se hasheará en el servicio
    User registerRequestDTOtoUser(RegisterRequestDTO registerRequestDTO);

    UserDTO userToUserDTO(User user);

    List<UserDTO> usersToUserDTOs(List<User> users);

    @Mapping(target = "passwordHash", ignore = true)
    void updateUserFromUserDTO(UserDTO userDTO, @MappingTarget User user);
}
