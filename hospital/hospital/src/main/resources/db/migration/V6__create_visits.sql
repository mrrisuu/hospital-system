CREATE TABLE visits (
                        id           BIGSERIAL PRIMARY KEY,
                        patient_id   BIGINT      NOT NULL REFERENCES patients (id),
                        doctor_id    BIGINT      NOT NULL REFERENCES doctors (id),
                        schedule_id  BIGINT      NOT NULL REFERENCES doctor_schedules (id),
                        visit_date   DATE        NOT NULL,
                        queue_number INTEGER     NOT NULL,
                        status       VARCHAR(20) NOT NULL,
                        complaint    VARCHAR(500),
                        created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_visit_status CHECK (status IN ('WAITING', 'CALLED', 'COMPLETED', 'CANCELLED')),
                        CONSTRAINT uq_visit_queue UNIQUE (doctor_id, visit_date, queue_number)
);

-- satu pasien hanya boleh punya satu kunjungan aktif per dokter per tanggal
CREATE UNIQUE INDEX uq_visit_patient_doctor_date
    ON visits (patient_id, doctor_id, visit_date)
    WHERE status <> 'CANCELLED';

CREATE INDEX idx_visits_patient_id ON visits (patient_id);
CREATE INDEX idx_visits_schedule_date ON visits (schedule_id, visit_date);