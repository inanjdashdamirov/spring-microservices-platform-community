package msp.community.gateway.filter;

import msp.community.gateway.exception.GatewayErrorWriter;
import msp.community.gateway.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private GatewayErrorWriter errorWriter;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    void filter_shouldAllowPublicAuthPaths() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/login").build()
        );

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();
    }

    @Test
    void filter_shouldRejectMissingAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/me").build()
        );

        when(errorWriter.write(any(ServerWebExchange.class), eq(HttpStatus.UNAUTHORIZED), eq("Unauthorized"), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();

        verify(errorWriter).write(any(ServerWebExchange.class), eq(HttpStatus.UNAUTHORIZED), eq("Unauthorized"), any());
    }

    @Test
    void filter_shouldAddUserHeadersForValidToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/me")
                        .header("Authorization", "Bearer valid-token")
                        .build()
        );

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("user@example.com");
        when(jwtService.extractRole("valid-token")).thenReturn("USER");

        StepVerifier.create(filter.filter(exchange, ex -> {
                    assert ex.getRequest().getHeaders().getFirst("X-User-Email").equals("user@example.com");
                    assert ex.getRequest().getHeaders().getFirst("X-User-Role").equals("USER");
                    return Mono.empty();
                }))
                .verifyComplete();
    }
}
