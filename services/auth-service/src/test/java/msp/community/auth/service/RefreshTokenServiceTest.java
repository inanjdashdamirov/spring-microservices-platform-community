package msp.community.auth.service;

import msp.community.auth.domain.RefreshToken;
import msp.community.auth.domain.User;
import msp.community.auth.exception.ApiException;
import msp.community.auth.exception.ErrorCode;
import msp.community.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(repository, 3_600_000L);
    }

    @Test
    void createRefreshToken_shouldPersistToken() {
        User user = new User();
        user.setEmail("user@example.com");

        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setToken("saved-token");
            return token;
        });

        String token = refreshTokenService.createRefreshToken(user);

        assertThat(token).isEqualTo("saved-token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().isRevoked()).isFalse();
    }

    @Test
    void validateRefreshToken_shouldRejectRevokedToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("token");
        refreshToken.setRevoked(true);
        refreshToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(repository.findByToken("token")).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("token"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void validateRefreshToken_shouldRejectExpiredToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("token");
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findByToken("token")).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("token"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED));
    }

    @Test
    void revokeRefreshToken_shouldMarkTokenRevoked() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("token");
        refreshToken.setRevoked(false);

        when(repository.findByToken("token")).thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeRefreshToken("token");

        assertThat(refreshToken.isRevoked()).isTrue();
        verify(repository).save(refreshToken);
    }
}
