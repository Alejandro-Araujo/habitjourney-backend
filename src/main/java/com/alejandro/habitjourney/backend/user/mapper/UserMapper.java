package com.alejandro.habitjourney.backend.user.mapper;


import com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO;
import com.alejandro.habitjourney.backend.user.dto.UserDTO;
import com.alejandro.habitjourney.backend.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interfaz Mapper de MapStruct para la conversión entre entidades {@link User}
 * y DTOs {@link UserDTO}.
 * Define los métodos de mapeo necesarios para la capa de presentación y servicio.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Mapea un {@link com.alejandro.habitjourney.backend.auth.dto.RegisterRequestDTO} a una entidad {@link com.alejandro.habitjourney.backend.user.model.User}.
     * La contraseña (passwordHash) se ignora porque se codificará por separado en el servicio.
     *
     * @param registerRequestDTO DTO de solicitud de registro.
     * @return Entidad User mapeada.
     */
    @Mapping(target = "passwordHash", ignore = true) // La contraseña se hasheará en el servicio
    User registerRequestDTOtoUser(RegisterRequestDTO registerRequestDTO);

    /**
     * Mapea una entidad {@link User} a un {@link UserDTO}.
     *
     * @param user Entidad User de origen.
     * @return DTO User mapeado.
     */
    UserDTO userToUserDTO(User user);

    /**
     * Mapea una lista de entidades {@link User} a una lista de {@link UserDTO}.
     *
     * @param users Lista de entidades User de origen.
     * @return Lista de DTOs User mapeados.
     */
    List<UserDTO> usersToUserDTOs(List<User> users);

    /**
     * Actualiza una entidad {@link User} existente
     * con los datos proporcionados en un {@link UserDTO}.
     * La contraseña (passwordHash) se ignora ya que el cambio se maneja en un método separado.
     *
     * @param userDTO DTO User con los datos de actualización.
     * @param user Entidad User a actualizar (objetivo del mapeo).
     */
    @Mapping(target = "passwordHash", ignore = true)
    void updateUserFromUserDTO(UserDTO userDTO, @MappingTarget User user);
}
