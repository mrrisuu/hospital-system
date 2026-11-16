CREATE SEQUENCE invoice_seq START 1;

CREATE TABLE invoices (
                          id             BIGSERIAL PRIMARY KEY,
                          invoice_number VARCHAR(20)   NOT NULL UNIQUE,
                          visit_id       BIGINT        NOT NULL UNIQUE REFERENCES visits (id),
                          total_amount   NUMERIC(12,2) NOT NULL,
                          paid_amount    NUMERIC(12,2) NOT NULL DEFAULT 0,
                          status         VARCHAR(20)   NOT NULL,
                          created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT chk_invoice_status CHECK (status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID')),
                          CONSTRAINT chk_invoice_amounts CHECK (
                              total_amount > 0 AND paid_amount >= 0 AND paid_amount <= total_amount)
);

CREATE TABLE invoice_items (
                               id          BIGSERIAL PRIMARY KEY,
                               invoice_id  BIGINT        NOT NULL REFERENCES invoices (id),
                               description VARCHAR(200)  NOT NULL,
                               quantity    INTEGER       NOT NULL CHECK (quantity > 0),
                               unit_price  NUMERIC(12,2) NOT NULL CHECK (unit_price > 0)
);

CREATE INDEX idx_invoice_items_invoice_id ON invoice_items (invoice_id);

CREATE TABLE payments (
                          id          BIGSERIAL PRIMARY KEY,
                          invoice_id  BIGINT        NOT NULL REFERENCES invoices (id),
                          amount      NUMERIC(12,2) NOT NULL CHECK (amount > 0),
                          method      VARCHAR(20)   NOT NULL CHECK (method IN ('CASH', 'TRANSFER', 'CARD')),
                          received_by BIGINT        NOT NULL REFERENCES users (id),
                          paid_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_invoice_id ON payments (invoice_id);