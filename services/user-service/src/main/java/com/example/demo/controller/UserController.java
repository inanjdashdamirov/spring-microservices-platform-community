package com.example.demo.controller;

import com.example.demo.dto.UpdateUserStatusRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal Jwt jwt) {
        return service.getCurrentUser(jwt.getSubject());
    }

    @Operation(summary = "List users (admin only)")
    @GetMapping
    public Page<UserResponseDTO> getAll(Pageable pageable) {
        return service.getAllUsers(pageable);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.getUserById(id, jwt.getSubject(), isAdmin(jwt));
    }

    @Operation(summary = "Update user status (admin only)")
    @PatchMapping("/{id}/status")
    public UserResponseDTO updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequestDTO dto
    ) {
        return service.updateUserStatus(id, dto.getStatus());
    }

    private boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaim("role"));
    }
}
