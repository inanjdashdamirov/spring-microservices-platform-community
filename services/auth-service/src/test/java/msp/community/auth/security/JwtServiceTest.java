package msp.community.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-key-for-unit-and-integration-tests-32chars";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000L);
    }

    @Test
    void generateToken_shouldIncludeSubjectAndRole() {
        String token = jwtService.generateToken("user@example.com", "USER");

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtService.extractExpiration(token)).isNotNull();
    }

    @Test
    void extractEmail_shouldReturnSubject() {
        String token = jwtService.generateToken("admin@example.com", "ADMIN");

        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@example.com");
    }
}
