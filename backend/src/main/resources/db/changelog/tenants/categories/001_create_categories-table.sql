-- liquibase formatted sql

-- changeset Cristóbal:categories-001-create-table
CREATE TABLE categories
(
    id               BIGINT                      NOT NULL,
    version          INTEGER                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by       VARCHAR(255)                NOT NULL,
    last_modified_by VARCHAR(255),
    name             VARCHAR(255)                NOT NULL,
    color            VARCHAR(7)                  NOT NULL,
    description      VARCHAR(255),
    is_default       BOOLEAN                     NOT NULL,
    parent_id        BIGINT,
    code             VARCHAR                     NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

-- changeset Cristóbal:categories-001-unique-name
ALTER TABLE categories
    ADD CONSTRAINT uc_categories_name UNIQUE (name);

-- changeset Cristóbal:categories-001-fk-parentId
ALTER TABLE categories
    ADD CONSTRAINT FK_CATEGORIES_ON_PARENT FOREIGN KEY (parent_id) REFERENCES categories (id);

