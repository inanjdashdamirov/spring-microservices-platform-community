CREATE SCHEMA IF NOT EXISTS user_schema;

SET search_path TO user_schema;

CREATE TABLE users
(
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    email  VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
);

INSERT INTO users (name, email, status)
VALUES ('Admin User', 'admin@example.com', 'ACTIVE');
