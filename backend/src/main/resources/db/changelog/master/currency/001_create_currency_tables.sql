-- liquibase formatted sql

-- changeset cnpe:1745862582796-2
CREATE TABLE supported_currencies
(
    id               BIGINT       NOT NULL,
    version          INTEGER      NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_by VARCHAR(255),
    code             VARCHAR(3)   NOT NULL,
    name             VARCHAR(255) NOT NULL,
    symbol           VARCHAR(255) NOT NULL,
    scale            INTEGER      NOT NULL,
    is_active        BOOLEAN      NOT NULL,
    CONSTRAINT pk_supported_currencies PRIMARY KEY (id)
);

-- changeset cnpe:1745862582796-3
ALTER TABLE supported_currencies
    ADD CONSTRAINT uc_supported_currencies_code UNIQUE (code);

