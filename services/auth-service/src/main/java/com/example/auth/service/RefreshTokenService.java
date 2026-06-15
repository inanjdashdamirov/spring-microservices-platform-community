package com.example.auth.service;

import com.example.auth.domain.RefreshToken;
import com.example.auth.domain.User;
import com.example.auth.exception.ApiException;
import com.example.auth.exception.ErrorCode;
import com.example.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshExpiration;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.repository = repository;
        this.refreshExpiration = refreshExpiration;
    }

    public String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        refreshToken.setRevoked(false);

        return repository.save(refreshToken).getToken();
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> ApiException.of(ErrorCode.INVALID_CREDENTIALS, "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw ApiException.of(ErrorCode.INVALID_CREDENTIALS, "Refresh token revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw ApiException.of(ErrorCode.TOKEN_EXPIRED, "Refresh token expired");
        }

        return refreshToken;
    }

    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> ApiException.of(ErrorCode.INVALID_CREDENTIALS, "Invalid refresh token"));

        refreshToken.setRevoked(true);

        repository.save(refreshToken);
    }
}
