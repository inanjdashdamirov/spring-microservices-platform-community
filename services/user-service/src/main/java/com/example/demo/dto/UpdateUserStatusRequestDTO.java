package com.example.demo.dto;

import com.example.demo.domain.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequestDTO {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
