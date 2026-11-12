CREATE SEQUENCE medical_record_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE patients (
                          id                    BIGSERIAL PRIMARY KEY,
                          user_id               BIGINT       NOT NULL UNIQUE REFERENCES users (id),
                          medical_record_number VARCHAR(20)  NOT NULL UNIQUE,
                          full_name             VARCHAR(100) NOT NULL,
                          birth_date            DATE         NOT NULL,
                          gender                VARCHAR(10)  NOT NULL,
                          phone                 VARCHAR(20)  NOT NULL,
                          address               VARCHAR(255),
                          created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT chk_patients_gender CHECK (gender IN ('MALE', 'FEMALE'))
);