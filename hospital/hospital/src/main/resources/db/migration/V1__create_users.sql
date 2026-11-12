CREATE TABLE users (
                       id         BIGSERIAL PRIMARY KEY,
                       email      VARCHAR(100) NOT NULL UNIQUE,
                       password   VARCHAR(100) NOT NULL,
                       role       VARCHAR(20)  NOT NULL,
                       active     BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'DOCTOR', 'STAFF', 'PATIENT'))
);