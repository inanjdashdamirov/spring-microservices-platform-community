package msp.community.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayErrorWriterTest {

    private final GatewayErrorWriter errorWriter = new GatewayErrorWriter(
            new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void write_shouldReturnJsonErrorBody() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/me")
                        .header("X-Correlation-Id", "corr-1")
                        .build()
        );

        StepVerifier.create(errorWriter.write(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "Missing token"))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType().toString()).contains("application/json");
    }
}
