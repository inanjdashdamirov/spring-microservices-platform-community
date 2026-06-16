package msp.community.auth.service;

import msp.community.auth.domain.Role;
import msp.community.auth.domain.User;
import msp.community.auth.dto.LoginRequestDTO;
import msp.community.auth.dto.RegisterRequestDTO;
import msp.community.auth.exception.ApiException;
import msp.community.auth.exception.ErrorCode;
import msp.community.auth.repository.RoleRepository;
import msp.community.auth.repository.UserRepository;
import msp.community.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSaveUserWhenEmailIsNew() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("new@example.com");
        dto.setPassword("password123");

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        authService.register(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new User()));

        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("dup@example.com");
        dto.setPassword("password123");

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
    }

    @Test
    void login_shouldReturnTokensForValidCredentials() {
        Role role = new Role();
        role.setName("ROLE_USER");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encoded");
        user.setRole(role);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("user@example.com");
        dto.setPassword("password123");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtService.generateToken("user@example.com", "USER")).thenReturn("access");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh");

        var response = authService.login(dto);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
    }

    @Test
    void login_shouldRejectInvalidPassword() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encoded");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("user@example.com");
        dto.setPassword("wrong");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void getCurrentUser_shouldReturnProfile() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(role);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        var me = authService.getCurrentUser("admin@example.com");

        assertThat(me.getEmail()).isEqualTo("admin@example.com");
        assertThat(me.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void logout_shouldRevokeRefreshToken() {
        authService.logout("token-value");

        verify(refreshTokenService).revokeRefreshToken("token-value");
    }
}
