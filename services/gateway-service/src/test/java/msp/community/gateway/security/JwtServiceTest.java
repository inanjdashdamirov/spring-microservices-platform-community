package msp.community.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-key-for-unit-and-integration-tests-32chars";

    private JwtService jwtService;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void isTokenValid_shouldAcceptValidToken() {
        String token = Jwts.builder()
                .subject("user@example.com")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
    }

    @Test
    void isTokenValid_shouldRejectMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }
}
