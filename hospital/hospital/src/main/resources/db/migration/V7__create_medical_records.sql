CREATE TABLE medical_records (
                                 id         BIGSERIAL PRIMARY KEY,
                                 visit_id   BIGINT        NOT NULL UNIQUE REFERENCES visits (id),
                                 symptoms   VARCHAR(500)  NOT NULL,
                                 diagnosis  VARCHAR(1000) NOT NULL,
                                 treatment  VARCHAR(1000),
                                 created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);