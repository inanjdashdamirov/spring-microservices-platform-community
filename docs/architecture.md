# Architecture

## Overview

Community v1.0 is a minimal but production-style microservices skeleton. All external requests pass through **Spring Cloud Gateway**. Services are isolated by data (schema per service) and communicate via REST.

## High-level diagram

```
Client
  |
  v
gateway-service :8080
  |
  +-- /api/auth/**    --> auth-service :8081  (/auth/**, StripPrefix)
  |
  +-- /api/users/**   --> user-service :8082
          |
          v
    PostgreSQL platform_db
      ├── auth_schema   (auth-service)
      └── user_schema   (user-service)
```

## Services

### gateway-service

- Single entry point for clients
- Routing: `/api/auth/**` → auth-service, `/api/users/**` → user-service
- JWT validation for protected routes (`/api/users/**`, `/api/auth/me`)
- Public auth paths: register, login, refresh-token, logout
- Correlation ID (`X-Correlation-Id`) on every request; gateway forwards the header to downstream services
- CORS (all origins in Community Edition)
- Global JSON error handling
- Actuator: `health`, `info`

### auth-service

- User registration and authentication
- Access and refresh JWT issuance
- `GET /auth/me` — account info (id, email, role)
- Logout with refresh token revocation in PostgreSQL
- Schema `auth_schema`: tables `roles`, `users`, `refresh_tokens`
- RBAC: `ROLE_USER` (default on registration) and `ROLE_ADMIN`
- Default admin: `admin@example.com` (local/dev only)
- Flyway migrations in `auth_schema`
- `CorrelationIdFilter` — read/generate `X-Correlation-Id`, MDC `correlationId` in logs

### user-service

- User business profiles (separate from auth)
- `GET /api/users/me` — profile by email from JWT (`sub`)
- `GET /api/users` — paginated list (**ADMIN**)
- `GET /api/users/{id}` — owner or **ADMIN**
- `PATCH /api/users/{id}/status` — status change (**ADMIN**)
- Statuses: `ACTIVE`, `INACTIVE`
- Schema `user_schema`: table `users`
- Profile is created manually or via Flyway seed; email must match JWT `sub`
- **OAuth2 Resource Server** — JWT validation (HS256, same `jwt.secret`)
- Flyway migrations in `user_schema`
- `CorrelationIdFilter` — read/generate `X-Correlation-Id`, MDC `correlationId` in logs

## Infrastructure

| Component | Purpose |
|-----------|---------|
| PostgreSQL (`platform_db`) | Single database, schemas `auth_schema` and `user_schema` |

## Architectural principles

- **API Gateway pattern** — clients do not call microservices directly
- **Schema per service** — logical data isolation within one PostgreSQL instance
- **Defense in depth** — gateway, auth-service (`/me`), and user-service independently validate JWT
- **Auth vs business profile** — auth-service stores credentials and roles; user-service stores business profile and status

## Correlation ID propagation

Clients may send the `X-Correlation-Id` header. If the header is missing or empty, the gateway generates a UUID.

```
Client --X-Correlation-Id--> gateway-service (MDC + response header)
                                  |
                                  +--X-Correlation-Id--> auth-service (MDC + response header)
                                  |
                                  +--X-Correlation-Id--> user-service (MDC + response header)
```

Log format in all services: `correlationId=<value>` (SLF4J MDC).

## Technology stack

- Java 21, Spring Boot 3.5.x, Spring Cloud Gateway
- Spring Security + JWT (jjwt)
- Spring Data JPA, Flyway, PostgreSQL
- Swagger/OpenAPI (springdoc)
- Docker Compose, Maven

## Professional edition (roadmap)

Kafka, Redis, Kubernetes, Prometheus, Grafana, and distributed tracing are planned for the paid Professional Edition.
