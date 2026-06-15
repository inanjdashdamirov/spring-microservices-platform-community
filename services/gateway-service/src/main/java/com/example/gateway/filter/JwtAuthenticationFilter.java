package com.example.gateway.filter;

import com.example.gateway.security.JwtService;
import com.example.gateway.exception.GatewayErrorWriter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final GatewayErrorWriter errorWriter;

    public JwtAuthenticationFilter(JwtService jwtService, GatewayErrorWriter errorWriter) {
        this.jwtService = jwtService;
        this.errorWriter = errorWriter;
    }

    @Override
    public Mono<Void> filter(
            org.springframework.web.server.ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain
    ) {

        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        log.info("JWT filter path={}, authHeader={}", path, authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return errorWriter.write(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "Missing or invalid Authorization header"
            );
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return errorWriter.write(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "Invalid or expired JWT token"
            );
        }

        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        var mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .build();

        return chain.filter(
                exchange.mutate()
                        .request(mutatedRequest)
                        .build()
        );
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator/")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/refresh-token")
                || path.equals("/api/auth/logout");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
