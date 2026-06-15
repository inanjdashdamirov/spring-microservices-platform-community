package msp.community.user.mapper;

import msp.community.user.domain.User;
import msp.community.user.dto.UserResponseDTO;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getStatus()
        );
    }
}
