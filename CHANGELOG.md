# Changelog

All notable changes to this project will be documented in this file.

## [1.0.12] - 2026-06-15

### Added

- GitHub Step 13: published public repository with description and topics

### Changed

- README: added repository URL
- `.gitignore`: explicit `**/target/` pattern

## [1.0.11] - 2026-06-15

### Changed

- Author and copyright updated to Inanj Dashdamirov

## [1.0.10] - 2026-06-15

### Changed

- Documentation Step 12: translated `docs/installation.md`, `docs/architecture.md`, and `docs/api-overview.md` to professional English
- `docs/roadmap.md` updated for documentation and error-model status

## [1.0.9] - 2026-06-15

### Changed

- README Step 11: rewritten as English product showcase with structured sections (Features, Architecture, Quick Start, API Examples, Community vs Professional, Roadmap)

## [1.0.8] - 2026-06-15

### Added

- Logging + Correlation ID Step 10: `CorrelationIdFilter` in auth-service
- Correlation ID propagation diagram in `docs/architecture.md`

### Changed

- Unified HTTP header `X-Correlation-Id` across gateway, auth-service, and user-service
- Console log pattern: `correlationId=%X{correlationId:-}` in all three services
- `docs/api-overview.md` and `docs/roadmap.md` updated for correlation ID

## [1.0.7] - 2026-06-15

### Added

- Error Handling Step 9: `ErrorCode`, `ApiErrorResponse`, `ApiException`, unified `GlobalExceptionHandler` in auth-service and user-service

### Changed

- Auth and user services return structured JSON errors with `errorCode` instead of plain text/maps

## [1.0.6] - 2026-06-15

### Added

- Docker Compose Step 7: documented canonical `up -d` / `down` commands

### Changed

- `docker-compose.yml`: removed obsolete `version`, container renamed to `postgres`
- README and installation guide updated with Docker Compose section

## [1.0.5] - 2026-06-15

### Added

- Database Step 6: single PostgreSQL `platform_db` with `auth_schema` and `user_schema`
- Squashed Flyway migrations; default roles `ROLE_USER`/`ROLE_ADMIN`
- Default admin seed: `admin@example.com` / `admin123` (local/dev only)

### Changed

- Docker Compose: removed second PostgreSQL container (`auth-postgres`)
- Both services connect to `platform_db` with schema-isolated Flyway

### Removed

- Separate `auth_db` and `demo_db` databases

## [1.0.4] - 2026-06-15

### Added

- User Service Step 5: `GET /api/users/me`, paginated `GET /api/users` (ADMIN), `PATCH /api/users/{id}/status` (ADMIN)
- `UserStatus` enum (`ACTIVE`, `INACTIVE`), Flyway V2 migration

### Changed

- `GET /api/users/{id}` — owner or ADMIN access check
- Removed `POST /api/users` and `DELETE /api/users/{id}` from Community API

### Removed

- User create/delete endpoints (out of Step 5 scope)

## [1.0.3] - 2026-06-15

### Added

- Auth Service Step 4: `GET /api/auth/me`, normalized `roles` + `users` schema, OAuth2 Resource Server on auth-service
- Flyway V3 migration: `auth_users` → `users` + `roles`, `refresh_tokens.user_id` FK

### Changed

- Refresh endpoint renamed: `/api/auth/refresh` → `/api/auth/refresh-token`
- Gateway JWT filter: only register, login, refresh-token, logout are public under `/api/auth/**`
- `/api/auth/me` requires JWT at gateway and auth-service

## [1.0.2] - 2026-06-15

### Added

- Gateway Step 3: routes `/api/auth/**` and `/api/users/**`, CORS, correlation ID + MDC, global JSON error handling
- OAuth2 Resource Server in user-service

### Changed

- API entry points now use `/api/auth/**` prefix through gateway (legacy `/auth/**` on gateway removed)
- Maven parent renamed to `spring-microservices-platform-community`

## [1.0.1] - 2026-06-15

### Changed

- Community stack cleanup: removed Kafka, Redis, Prometheus, Tracing, Eureka/Config
- Logout simplified to refresh token revocation only (no access token blacklist)
- Docker Compose now runs only PostgreSQL + 3 microservices
- Documentation updated to reflect Community v1.0 technology stack

## [1.0.0] - 2026-06-15

### Added

- Community edition with three core services: `gateway-service`, `auth-service`, `user-service`
- Docker Compose local environment
- JWT authentication with refresh tokens
- Basic documentation and project structure

### Changed

- Restructured repository layout: `services/`, `docker/`, `docs/`, `scripts/`

### Removed

- Account, payment, audit, notification, config, and discovery services from Community edition
- Kubernetes manifests and Prometheus configuration (moved to Professional roadmap)
