-- liquibase formatted sql

-- changeset cnpe:1746315399791-1
CREATE SEQUENCE IF NOT EXISTS primary_sequence START WITH 10000 INCREMENT BY 1;

-- changeset cnpe:1746315399791-2
CREATE TABLE customers
(
    id                   BIGINT                      NOT NULL,
    version              INTEGER                     NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by           VARCHAR(255)                NOT NULL,
    last_modified_by     VARCHAR(255),
    email                VARCHAR(100),
    phone                VARCHAR(25),
    total_orders         INTEGER                     NOT NULL,
    last_order_date      TIMESTAMP WITHOUT TIME ZONE,
    notes                VARCHAR(500),
    is_active            BOOLEAN                     NOT NULL,
    customer_first_name  VARCHAR(50)                 NOT NULL,
    customer_last_name   VARCHAR(50),
    customer_tax_id      VARCHAR(50),
    address_street       VARCHAR(100),
    address_city         VARCHAR(50),
    address_country      VARCHAR(50),
    address_postal_code  VARCHAR(20),
    total_spent_amount   DECIMAL                     NOT NULL,
    total_spent_currency VARCHAR(3)                  NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id)
);

-- changeset cnpe:1746315399791-3
ALTER TABLE customers
    ADD CONSTRAINT uc_customers_customer_tax UNIQUE (customer_tax_id);

-- changeset cnpe:1746315399791-4
ALTER TABLE customers
    ADD CONSTRAINT uc_customers_email UNIQUE (email);

-- changeset cnpe:1746315399791-5
CREATE INDEX idx_customer_email ON customers (email);

-- changeset cnpe:1746315399791-6
CREATE INDEX idx_customer_phone ON customers (phone);

-- changeset cnpe:1746315399791-7
CREATE UNIQUE INDEX idx_customer_tax_id ON customers (customer_tax_id);

