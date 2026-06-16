package msp.community.user.mapper;

import msp.community.user.domain.User;
import msp.community.user.domain.UserStatus;
import msp.community.user.dto.UserResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toDTO_shouldMapAllFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setStatus(UserStatus.ACTIVE);

        UserResponseDTO dto = UserMapper.toDTO(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Admin User");
        assertThat(dto.getEmail()).isEqualTo("admin@example.com");
        assertThat(dto.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
