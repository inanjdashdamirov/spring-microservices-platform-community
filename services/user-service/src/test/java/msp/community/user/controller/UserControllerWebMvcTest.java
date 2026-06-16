package msp.community.user.controller;

import msp.community.user.config.SecurityConfig;
import msp.community.user.domain.UserStatus;
import msp.community.user.dto.UserResponseDTO;
import msp.community.user.exception.GlobalExceptionHandler;
import msp.community.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void me_shouldReturnCurrentUser() throws Exception {
        when(userService.getCurrentUser("user@example.com"))
                .thenReturn(new UserResponseDTO(1L, "User", "user@example.com", UserStatus.ACTIVE));

        mockMvc.perform(get("/api/users/me")
                        .with(jwt().jwt(j -> j.subject("user@example.com").claim("role", "USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void listUsers_shouldRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(jwt().jwt(j -> j.subject("user@example.com").claim("role", "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_shouldAllowAdmin() throws Exception {
        when(userService.getAllUsers(any())).thenReturn(
                new PageImpl<>(List.of(new UserResponseDTO(1L, "Admin User", "admin@example.com", UserStatus.ACTIVE)))
        );

        mockMvc.perform(get("/api/users")
                        .with(jwt()
                                .jwt(j -> j.subject("admin@example.com").claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("admin@example.com"));
    }

    @Test
    void getById_shouldReturnUserForAdmin() throws Exception {
        when(userService.getUserById(eq(1L), eq("admin@example.com"), eq(true)))
                .thenReturn(new UserResponseDTO(1L, "Admin User", "admin@example.com", UserStatus.ACTIVE));

        mockMvc.perform(get("/api/users/1")
                        .with(jwt()
                                .jwt(j -> j.subject("admin@example.com").claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateStatus_shouldAllowAdmin() throws Exception {
        when(userService.updateUserStatus(1L, UserStatus.INACTIVE))
                .thenReturn(new UserResponseDTO(1L, "Admin User", "admin@example.com", UserStatus.INACTIVE));

        mockMvc.perform(patch("/api/users/1/status")
                        .with(jwt()
                                .jwt(j -> j.subject("admin@example.com").claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"INACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}
