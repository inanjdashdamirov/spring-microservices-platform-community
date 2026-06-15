# Installation Guide

## Prerequisites

| Tool | Version |
|------|---------|
| Docker Desktop | latest |
| Docker Compose | v2+ |
| JDK (optional, for local build) | 21 |
| Maven (optional) | 3.9+ or use `./mvnw` |

## Docker Compose

File: `docker/docker-compose.yml`

| Container | Port | Description |
|-----------|------|-------------|
| `postgres` | 5432 | PostgreSQL (`platform_db`) |
| `gateway-service` | 8080 | API Gateway |
| `auth-service` | 8081 | Authentication |
| `user-service` | 8082 | User profiles |

## Quick start (Docker)

1. Clone or unpack the repository.
2. From the project root, build JARs and start containers:

```bash
./mvnw package -DskipTests
docker compose -f docker/docker-compose.yml up -d --build
```

Subsequent runs without rebuild:

```bash
docker compose -f docker/docker-compose.yml up -d
```

3. Wait for containers to start (1–3 minutes on first build).
4. Verify the gateway:

```bash
curl http://localhost:8080/actuator/health
```

## Stop

```bash
docker compose -f docker/docker-compose.yml down
```

Alternative — helper scripts:

```bash
chmod +x scripts/start.sh scripts/stop.sh
./scripts/start.sh   # up -d --build
./scripts/stop.sh    # down
```

## Local build (without Docker)

```bash
./mvnw compile -DskipTests
```

To run individual services locally, start PostgreSQL (e.g. via Docker Compose for infrastructure only) and configure `application.yaml` with `localhost` instead of Docker service hostnames.

## Ports

| Service | Port |
|---------|------|
| Gateway | 8080 |
| Auth | 8081 |
| User | 8082 |
| PostgreSQL | 5432 |

## Troubleshooting

- **Ports in use** — stop conflicting processes or change port mappings in `docker/docker-compose.yml`.
- **Service fails to start** — `docker compose -f docker/docker-compose.yml logs <service-name>`.
- **Flyway schema change** — `docker compose -f docker/docker-compose.yml down -v` and restart.
