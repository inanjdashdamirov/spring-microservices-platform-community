#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker/docker-compose.yml"

echo "Starting Spring Microservices Platform (Community v1.0)..."
docker compose -f "${COMPOSE_FILE}" up -d --build

echo ""
echo "Platform is starting. Gateway: http://localhost:8080"
echo "Auth service:  http://localhost:8081"
echo "User service:  http://localhost:8082"
