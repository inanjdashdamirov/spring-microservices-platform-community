# Spring Microservices Platform Community Edition

**Repository:** https://github.com/inanjdashdamirov/spring-microservices-platform-community

Production-style microservices starter kit on **Java 21** and **Spring Boot 3**. Community Edition provides a working foundation: API Gateway, JWT authentication, and user management — ready to run locally with Docker Compose.

**Base URL:** `http://localhost:8080`

---

## What is this?

Spring Microservices Platform is a **reference architecture** for building secure, observable microservices with Spring. Community Edition is the free, open-source baseline. It demonstrates patterns used in production — gateway routing, JWT auth, schema-per-service, structured errors, and correlation ID propagation — without the operational complexity of Kafka, Kubernetes, or distributed tracing.

All external traffic goes through the **API Gateway**. Clients never call auth-service or user-service directly.

Use it to:

- Learn microservices patterns with real, runnable code
- Bootstrap a new product backend
- Compare against the upcoming **Professional Edition** (paid)

Further reading: [Architecture](docs/architecture.md) · [Installation](docs/installation.md) · [API Overview](docs/api-overview.md)

---

## Features

- **API Gateway** — single entry point, JWT validation, CORS, global error handling
- **JWT authentication** — register, login, refresh token, logout, `/auth/me`
- **User management** — business profiles, RBAC (`USER` / `ADMIN`), pagination, status updates
- **Schema per service** — one PostgreSQL instance, isolated `auth_schema` and `user_schema`
- **Structured API errors** — unified `errorCode` in auth and user services
- **Correlation ID** — `X-Correlation-Id` propagated gateway → auth → user; `correlationId=` in logs
- **Flyway migrations** — versioned database schema for both services
- **Swagger/OpenAPI** — interactive docs on auth and user services
- **Docker Compose** — full stack in four containers

---

## Architecture

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

**Principles:** API Gateway pattern, schema-per-service, defense in depth (JWT checked at gateway and services), separation of auth credentials vs business profile.

Details: [docs/architecture.md](docs/architecture.md)

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.x, Spring Cloud Gateway |
| Security | Spring Security, JWT (jjwt), OAuth2 Resource Server |
| Data | PostgreSQL, Spring Data JPA, Flyway |
| API docs | springdoc OpenAPI |
| Build & run | Maven, Docker Compose |

---

## Services

| Service | Port | Responsibility |
|---------|------|----------------|
| `gateway-service` | 8080 | Routing `/api/auth/**`, `/api/users/**`; JWT; CORS; correlation ID |
| `auth-service` | 8081 | Registration, login, tokens, roles, `/auth/me` |
| `user-service` | 8082 | User profiles, admin list/status, `/api/users/me` |

**Swagger UI (direct access):**

| Service | URL |
|---------|-----|
| Auth | http://localhost:8081/swagger-ui/index.html |
| User | http://localhost:8082/swagger-ui/index.html |

---

## Quick Start

**Requirements:** Docker Desktop, Docker Compose v2

```bash
# From project root
./mvnw package -DskipTests
docker compose -f docker/docker-compose.yml up -d --build
```

Platform is available at `http://localhost:8080`.

```bash
# Health check
curl http://localhost:8080/actuator/health

# Stop
docker compose -f docker/docker-compose.yml down

# Reset database after schema changes
docker compose -f docker/docker-compose.yml down -v
```

**Default admin (local/dev only):**

| Email | Password |
|-------|----------|
| admin@example.com | admin123 |

> Never use default credentials in staging or production.

Alternative: `./scripts/start.sh` and `./scripts/stop.sh` (wrappers with `--build`).

More: [docs/installation.md](docs/installation.md)

---

## API Examples

All requests go through the gateway.

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'
```

### Current user

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

### List users (ADMIN)

```bash
curl "http://localhost:8080/api/users?page=0&size=10" \
  -H "Authorization: Bearer <adminAccessToken>"
```

### Correlation ID

```bash
curl -H "X-Correlation-Id: my-trace-id" \
  http://localhost:8080/actuator/health
```

Full reference: [docs/api-overview.md](docs/api-overview.md)

---

## Project Structure

```
spring-microservices-platform-community-v.1.0/
├── services/
│   ├── gateway-service/
│   ├── auth-service/
│   └── user-service/
├── docker/
│   └── docker-compose.yml
├── docs/
│   ├── architecture.md
│   ├── api-overview.md
│   ├── installation.md
│   └── roadmap.md
├── scripts/
├── CHANGELOG.md
├── LICENSE
└── README.md
```

---

## What is included in Community Edition

| Included | Professional Edition (planned) |
|----------|-------------------------------|
| Gateway, Auth, User services | Account, Payment, Audit, Notification services |
| JWT + refresh tokens | Kubernetes / Helm |
| PostgreSQL + Flyway | Redis, Kafka |
| Docker Compose local stack | Prometheus, Grafana |
| Swagger, structured errors | Distributed tracing (Zipkin/Jaeger) |
| Correlation ID in logs | Spring Cloud Config |
| MIT license, full source | Commercial support and extended docs |

---

## Professional Edition Roadmap

**v1.1 — Stability**

- End-to-end smoke tests
- Unified error model across all layers
- `.env.example`, Postman collection

**v1.2 — Professional Edition**

- Additional microservices
- Kubernetes manifests
- Observability stack
- Centralized configuration

**v2.0 — Commercial launch**

- Full documentation pack, CI/CD examples, sales channels

Details: [docs/roadmap.md](docs/roadmap.md)

---

## License

MIT License — see [LICENSE](LICENSE).

Copyright (c) 2026 Inanj Dashdamirov

---

## Author

**Inanj Dashdamirov**

- GitHub: [@inanjdashdamirov](https://github.com/inanjdashdamirov)
- Email: inanjdashdamirov@gmail.com

Built as part of the Spring Microservices Platform product line (Community → Professional → Architect).
