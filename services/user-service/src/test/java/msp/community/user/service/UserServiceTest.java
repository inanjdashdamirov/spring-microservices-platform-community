package msp.community.user.service;

import msp.community.user.domain.User;
import msp.community.user.domain.UserStatus;
import msp.community.user.exception.ApiException;
import msp.community.user.exception.ErrorCode;
import msp.community.user.exception.UserNotFoundException;
import msp.community.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUser_shouldReturnProfile() {
        User user = new User();
        user.setId(1L);
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        var dto = userService.getCurrentUser("admin@example.com");

        assertThat(dto.getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void getCurrentUser_shouldThrowWhenMissing() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserById_shouldDenyAccessForNonOwnerNonAdmin() {
        User user = new User();
        user.setId(2L);
        user.setEmail("other@example.com");

        when(repository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getUserById(2L, "user@example.com", false))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void getUserById_shouldAllowAdmin() {
        User user = new User();
        user.setId(2L);
        user.setEmail("other@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(repository.findById(2L)).thenReturn(Optional.of(user));

        var dto = userService.getUserById(2L, "admin@example.com", true);

        assertThat(dto.getEmail()).isEqualTo("other@example.com");
    }

    @Test
    void getAllUsers_shouldReturnPage() {
        User user = new User();
        user.setId(1L);
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(repository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(user)));

        var page = userService.getAllUsers(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateUserStatus_shouldPersistChange() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        var dto = userService.updateUserStatus(1L, UserStatus.INACTIVE);

        assertThat(dto.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(repository).save(user);
    }
}
