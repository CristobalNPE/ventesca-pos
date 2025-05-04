-- liquibase formatted sql

-- changeset cnpe:1746325893726-2
CREATE TABLE cash_movements
(
    id                 BIGINT                      NOT NULL,
    version            INTEGER                     NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by         VARCHAR(255)                NOT NULL,
    last_modified_by   VARCHAR(255),
    session_id         BIGINT                      NOT NULL,
    movement_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type               VARCHAR(255)                NOT NULL,
    reason             VARCHAR(255)                NOT NULL,
    notes              VARCHAR(255),
    user_idp_id        VARCHAR(255)                NOT NULL,
    amount             DECIMAL                     NOT NULL,
    currency           VARCHAR(3)                  NOT NULL,
    CONSTRAINT pk_cash_movements PRIMARY KEY (id)
);

-- changeset cnpe:1746325893726-3
CREATE TABLE register_sessions
(
    id                            BIGINT                      NOT NULL,
    version                       INTEGER                     NOT NULL,
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by                    VARCHAR(255)                NOT NULL,
    last_modified_by              VARCHAR(255),
    session_number                VARCHAR(255)                NOT NULL,
    branch_id                     BIGINT                      NOT NULL,
    user_idp_id                   VARCHAR(255)                NOT NULL,
    register_id                   VARCHAR(255),
    status                        VARCHAR(255)                NOT NULL,
    opening_time                  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    closing_time                  TIMESTAMP WITHOUT TIME ZONE,
    notes                         VARCHAR(500),
    opening_float_amount          DECIMAL                     NOT NULL,
    opening_float_currency        VARCHAR(3)                  NOT NULL,
    closing_counted_cash_amount   DECIMAL,
    closing_counted_cash_currency VARCHAR(3),
    calc_cash_sales_amount        DECIMAL,
    calc_cash_sales_currency      VARCHAR(3),
    calc_cash_refunds_amount      DECIMAL,
    calc_cash_refunds_currency    VARCHAR(3),
    calc_pay_ins_amount           DECIMAL,
    calc_pay_ins_currency         VARCHAR(3),
    calc_pay_outs_amount          DECIMAL,
    calc_pay_outs_currency        VARCHAR(3),
    calc_expected_cash_amount     DECIMAL,
    calc_expected_cash_currency   VARCHAR(3),
    cash_variance_amount          DECIMAL,
    cash_variance_currency        VARCHAR(3),
    total_sales_amount            DECIMAL,
    total_sales_currency          VARCHAR(3),
    total_refund_amount           DECIMAL,
    total_refund_currency         VARCHAR(3),
    CONSTRAINT pk_register_sessions PRIMARY KEY (id)
);

-- changeset cnpe:1746325893726-4
ALTER TABLE register_sessions
    ADD CONSTRAINT uc_register_sessions_session_number UNIQUE (session_number);

-- changeset cnpe:1746325893726-5
CREATE INDEX idx_session_branch_id ON register_sessions (branch_id);

-- changeset cnpe:1746325893726-6
CREATE INDEX idx_session_opening_time ON register_sessions (opening_time);

-- changeset cnpe:1746325893726-7
CREATE INDEX idx_session_user_branch_status ON register_sessions (user_idp_id, branch_id, status);

-- changeset cnpe:1746325893726-8
ALTER TABLE cash_movements
    ADD CONSTRAINT FK_CASH_MOVEMENTS_ON_SESSION FOREIGN KEY (session_id) REFERENCES register_sessions (id);

