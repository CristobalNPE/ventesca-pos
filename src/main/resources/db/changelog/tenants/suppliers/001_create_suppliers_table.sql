-- liquibase formatted sql

-- changeset Cristóbal:1745627005014-2
CREATE TABLE suppliers
(
    id                         BIGINT                      NOT NULL,
    version                    INTEGER                     NOT NULL,
    created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by                 VARCHAR(255)                NOT NULL,
    last_modified_by           VARCHAR(255),
    business_name              VARCHAR(255)                NOT NULL,
    is_active                  BOOLEAN                     NOT NULL,
    is_default                 BOOLEAN                     NOT NULL,
    representative_name        VARCHAR(50),
    representative_last_name   VARCHAR(50),
    representative_personal_id VARCHAR(50),
    phone_number               VARCHAR(20),
    email                      VARCHAR(255),
    website                    VARCHAR(100),
    street                     VARCHAR(100),
    city                       VARCHAR(50),
    country                    VARCHAR(50),
    postal_code                VARCHAR(20),
    CONSTRAINT pk_suppliers PRIMARY KEY (id)
);

