package msp.community.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdGatewayFilterTest {

    private final CorrelationIdGatewayFilter filter = new CorrelationIdGatewayFilter();

    @Test
    void filter_shouldPropagateIncomingCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/me")
                        .header("X-Correlation-Id", "trace-123")
                        .build()
        );

        StepVerifier.create(filter.filter(exchange, ex -> {
                    assertThat(ex.getRequest().getHeaders().getFirst("X-Correlation-Id")).isEqualTo("trace-123");
                    assertThat(ex.getResponse().getHeaders().getFirst("X-Correlation-Id")).isEqualTo("trace-123");
                    return Mono.empty();
                }))
                .verifyComplete();
    }

    @Test
    void filter_shouldGenerateCorrelationIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/me").build()
        );

        StepVerifier.create(filter.filter(exchange, ex -> {
                    String correlationId = ex.getRequest().getHeaders().getFirst("X-Correlation-Id");
                    assertThat(correlationId).isNotBlank();
                    assertThat(ex.getResponse().getHeaders().getFirst("X-Correlation-Id")).isEqualTo(correlationId);
                    return Mono.empty();
                }))
                .verifyComplete();
    }
}
