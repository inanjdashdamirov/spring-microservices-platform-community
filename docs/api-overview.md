# API Overview

Base URL via gateway: `http://localhost:8080`

All client requests go through the gateway. Auth endpoints are available under the `/api/auth` prefix.

## Correlation ID

Optional header `X-Correlation-Id` for tracing requests through the gateway and downstream services. If omitted, the gateway generates a UUID and returns it in the response header. All service logs include `correlationId=<value>`.

```bash
curl -H "X-Correlation-Id: my-trace-id" http://localhost:8080/actuator/health
```

## Authentication (`/api/auth/**`)

| Method | Path | Description | JWT |
|--------|------|-------------|-----|
| POST | `/api/auth/register` | Register a user | not required |
| POST | `/api/auth/login` | Login, obtain access + refresh token | not required |
| POST | `/api/auth/refresh-token` | Refresh access token | not required |
| POST | `/api/auth/logout` | Logout, revoke refresh token | not required |
| GET | `/api/auth/me` | Current user (id, email, role) | **required** |

### Login example

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'
```

Response (example):

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

### Me example

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

Response (example):

```json
{
  "id": 1,
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

### Refresh token example

```bash
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

### Logout example

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

> The access token remains valid until its TTL expires (15 min). The refresh token cannot be used after logout.

## Users (`/api/users/**`)

Required header: `Authorization: Bearer <accessToken>`.

> **Business profile** is stored in `user_schema` (user-service), separate from the account in `auth_schema`. The profile is created manually (SQL or Flyway migration); `email` must match JWT `sub`. For local/dev, a seed profile exists for `admin@example.com`. If no profile exists, `GET /api/users/me` returns **404**.

| Method | Path | Description | Roles |
|--------|------|-------------|-------|
| GET | `/api/users/me` | Current user profile | authenticated |
| GET | `/api/users` | User list (paginated) | ADMIN |
| GET | `/api/users/{id}` | User by ID | owner or ADMIN |
| PATCH | `/api/users/{id}/status` | Change status (`ACTIVE` / `INACTIVE`) | ADMIN |

Query parameters for list: `page` (default 0), `size` (default 20).

### Get current profile example

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <accessToken>"
```

Response (example):

```json
{
  "id": 1,
  "name": "Admin User",
  "email": "admin@example.com",
  "status": "ACTIVE"
}
```

### List users example (admin)

```bash
curl "http://localhost:8080/api/users?page=0&size=10" \
  -H "Authorization: Bearer <adminAccessToken>"
```

### Update status example (admin)

```bash
curl -X PATCH http://localhost:8080/api/users/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <adminAccessToken>" \
  -d '{"status":"INACTIVE"}'
```

## Service error response (auth / user)

Auth-service and user-service return a unified JSON body with an `errorCode` field:

```json
{
  "timestamp": "2026-06-15T14:00:00Z",
  "status": 401,
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Invalid credentials",
  "path": "/auth/login",
  "details": null
}
```

Codes: `USER_NOT_FOUND`, `INVALID_CREDENTIALS`, `EMAIL_ALREADY_EXISTS`, `ACCESS_DENIED`, `TOKEN_EXPIRED`, `VALIDATION_ERROR`, `INTERNAL_ERROR`.

For `VALIDATION_ERROR`, the `details` field contains field errors, e.g. `{"email": "must not be blank"}`.

## Gateway error response

Gateway errors (e.g. 401 without JWT) return JSON:

```json
{
  "timestamp": "2026-06-15T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header",
  "path": "/api/users",
  "correlationId": "uuid"
}
```

## Health & Actuator

| Service | Health endpoint |
|---------|-----------------|
| Gateway | `http://localhost:8080/actuator/health` |
| Auth | `http://localhost:8081/actuator/health` |
| User | `http://localhost:8082/actuator/health` |

## OpenAPI

Swagger UI is available on backend services (direct access):

- Auth: `http://localhost:8081/swagger-ui/index.html`
- User: `http://localhost:8082/swagger-ui/index.html` (use **Authorize** for Bearer JWT)
