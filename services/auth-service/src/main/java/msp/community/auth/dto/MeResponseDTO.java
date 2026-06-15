package msp.community.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeResponseDTO {

    private Long id;
    private String email;
    private String role;
}
