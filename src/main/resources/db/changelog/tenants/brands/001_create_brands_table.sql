-- liquibase formatted sql

-- changeset Cristóbal:brands-001-create-table
CREATE TABLE brands
(
    id               BIGINT                      NOT NULL,
    version          INTEGER                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by       VARCHAR(255)                NOT NULL,
    last_modified_by VARCHAR(255),
    name             VARCHAR(255)                NOT NULL,
    is_default       BOOLEAN                     NOT NULL,
    code             VARCHAR                     NOT NULL,
    CONSTRAINT pk_brands PRIMARY KEY (id)
);

