SET search_path TO auth_schema;

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

INSERT INTO users (email, password, role_id)
VALUES (
    'admin@example.com',
    '$2a$10$aSaLstuGnWYzCYudOUf1Vusq5GAC/McDQyZQZUtu/PhzKqkWSJtrO',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);
