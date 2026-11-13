CREATE TABLE polis (
                       id          BIGSERIAL PRIMARY KEY,
                       name        VARCHAR(100) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctors (
                         id             BIGSERIAL PRIMARY KEY,
                         user_id        BIGINT       NOT NULL UNIQUE REFERENCES users (id),
                         poli_id        BIGINT       NOT NULL REFERENCES polis (id),
                         full_name      VARCHAR(100) NOT NULL,
                         license_number VARCHAR(50)  NOT NULL UNIQUE,
                         phone          VARCHAR(20)  NOT NULL,
                         created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctors_poli_id ON doctors (poli_id);

CREATE TABLE staff (
                       id         BIGSERIAL PRIMARY KEY,
                       user_id    BIGINT       NOT NULL UNIQUE REFERENCES users (id),
                       full_name  VARCHAR(100) NOT NULL,
                       phone      VARCHAR(20)  NOT NULL,
                       created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);