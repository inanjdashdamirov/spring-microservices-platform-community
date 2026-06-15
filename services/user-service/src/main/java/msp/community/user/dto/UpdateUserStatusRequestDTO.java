package msp.community.user.dto;

import msp.community.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequestDTO {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
