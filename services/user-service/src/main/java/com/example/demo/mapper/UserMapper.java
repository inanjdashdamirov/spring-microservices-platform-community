package com.example.demo.mapper;

import com.example.demo.domain.User;
import com.example.demo.dto.UserResponseDTO;

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
