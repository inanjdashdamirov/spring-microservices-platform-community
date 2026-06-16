package msp.community.gateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayIntegrationTest {

    private static final String SECRET = "test-jwt-secret-key-for-unit-and-integration-tests-32chars";

    private static final WireMockServer WIRE_MOCK = new WireMockServer(
            WireMockConfiguration.options().dynamicPort());

    static {
        WIRE_MOCK.start();
        WireMock.configureFor("localhost", WIRE_MOCK.port());
    }

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void gatewayRoutes(DynamicPropertyRegistry registry) {
        registry.add("test.downstream.url", () -> "http://localhost:" + WIRE_MOCK.port());
    }

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK.stop();
    }

    @BeforeEach
    void stubDownstream() {
        WireMock.reset();
        WireMock.stubFor(WireMock.post(urlPathEqualTo("/auth/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("""
                                {"accessToken":"downstream","refreshToken":"refresh"}
                                """)));

        WireMock.stubFor(WireMock.get(urlPathEqualTo("/api/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("""
                                {"id":1,"name":"User","email":"user@example.com","status":"ACTIVE"}
                                """)));
    }

    @Test
    void publicAuthRoute_shouldProxyToDownstream() {
        webTestClient.post()
                .uri("/api/auth/login")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue("""
                        {"email":"user@example.com","password":"password123"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("downstream");

        WireMock.verify(postRequestedFor(urlEqualTo("/auth/login")));
    }

    @Test
    void protectedRoute_shouldRejectWithoutToken() {
        webTestClient.get()
                .uri("/api/users/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header");
    }

    @Test
    void protectedRoute_shouldProxyWithValidToken() {
        String token = buildToken("user@example.com", "USER");

        webTestClient.get()
                .uri("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Correlation-Id", "trace-abc")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Correlation-Id", "trace-abc")
                .expectBody()
                .jsonPath("$.email").isEqualTo("user@example.com");

        WireMock.verify(getRequestedFor(urlEqualTo("/api/users/me"))
                .withHeader("X-User-Email", equalTo("user@example.com"))
                .withHeader("X-User-Role", equalTo("USER")));
    }

    private String buildToken(String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
