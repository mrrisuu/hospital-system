CREATE TABLE doctor_schedules (
                                  id          BIGSERIAL PRIMARY KEY,
                                  doctor_id   BIGINT      NOT NULL REFERENCES doctors (id),
                                  day_of_week VARCHAR(10) NOT NULL,
                                  start_time  TIME        NOT NULL,
                                  end_time    TIME        NOT NULL,
                                  quota       INTEGER     NOT NULL,
                                  created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT chk_schedule_day CHECK (day_of_week IN
                                                                     ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
                                  CONSTRAINT chk_schedule_time CHECK (end_time > start_time),
                                  CONSTRAINT chk_schedule_quota CHECK (quota > 0)
);

CREATE INDEX idx_doctor_schedules_doctor_id ON doctor_schedules (doctor_id);