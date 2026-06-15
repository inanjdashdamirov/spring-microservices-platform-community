CREATE SCHEMA IF NOT EXISTS auth_schema;

SET search_path TO auth_schema;

CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id  BIGINT       NOT NULL REFERENCES roles (id)
);

CREATE TABLE refresh_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(255)   NOT NULL UNIQUE,
    user_id    BIGINT         NOT NULL REFERENCES users (id),
    expires_at TIMESTAMP      NOT NULL,
    revoked    BOOLEAN        NOT NULL DEFAULT FALSE
);
