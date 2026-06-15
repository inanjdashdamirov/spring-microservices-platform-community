package msp.community.auth.service;

import msp.community.auth.domain.Role;
import msp.community.auth.domain.User;
import msp.community.auth.dto.AuthResponseDTO;
import msp.community.auth.dto.LoginRequestDTO;
import msp.community.auth.dto.MeResponseDTO;
import msp.community.auth.dto.RegisterRequestDTO;
import msp.community.auth.exception.ApiException;
import msp.community.auth.exception.ErrorCode;
import msp.community.auth.repository.RoleRepository;
import msp.community.auth.repository.UserRepository;
import msp.community.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public void register(RegisterRequestDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw ApiException.of(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> ApiException.of(ErrorCode.INTERNAL_ERROR, "Default role not found"));

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(userRole);

        userRepository.save(user);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> ApiException.of(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw ApiException.of(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getEmail(), toJwtRole(user.getRole().getName()));
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    public AuthResponseDTO refresh(String refreshTokenValue) {
        var refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user.getEmail(), toJwtRole(user.getRole().getName()));

        return new AuthResponseDTO(newAccessToken, refreshTokenValue);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    public MeResponseDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.of(ErrorCode.USER_NOT_FOUND, "User not found"));

        return new MeResponseDTO(user.getId(), user.getEmail(), toJwtRole(user.getRole().getName()));
    }

    private String toJwtRole(String dbRole) {
        return dbRole.startsWith("ROLE_") ? dbRole.substring(5) : dbRole;
    }
}
