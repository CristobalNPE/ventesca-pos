-- liquibase formatted sql

-- changeset Cristóbal:1745727255491-2
CREATE TABLE inventory_items
(
    id                  BIGINT                      NOT NULL,
    version             INTEGER                     NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)                NOT NULL,
    last_modified_by    VARCHAR(255),
    branch_id           BIGINT                      NOT NULL,
    product_id          BIGINT                      NOT NULL,
    stock_quantity      DOUBLE PRECISION,
    minimum_stock_level DOUBLE PRECISION,
    unit_of_measure     VARCHAR(255),
    CONSTRAINT pk_inventory_items PRIMARY KEY (id)
);

-- changeset Cristóbal:1745727255491-3
CREATE TABLE stock_modifications
(
    id               BIGINT                      NOT NULL,
    version          INTEGER                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by       VARCHAR(255)                NOT NULL,
    last_modified_by VARCHAR(255),
    amount           DOUBLE PRECISION            NOT NULL,
    type             VARCHAR(255)                NOT NULL,
    reason           VARCHAR(255)                NOT NULL,
    item_id          BIGINT                      NOT NULL,
    CONSTRAINT pk_stock_modifications PRIMARY KEY (id)
);

-- changeset Cristóbal:1745727255491-4
CREATE INDEX idx_invitem_branch_id ON inventory_items (branch_id);

-- changeset Cristóbal:1745727255491-5
CREATE UNIQUE INDEX idx_invitem_product_branch ON inventory_items (product_id, branch_id);

-- changeset Cristóbal:1745727255491-6
CREATE INDEX idx_invitem_product_id ON inventory_items (product_id);

-- changeset Cristóbal:1745727255491-7
ALTER TABLE stock_modifications
    ADD CONSTRAINT FK_STOCK_MODIFICATIONS_ON_ITEM FOREIGN KEY (item_id) REFERENCES inventory_items (id);

