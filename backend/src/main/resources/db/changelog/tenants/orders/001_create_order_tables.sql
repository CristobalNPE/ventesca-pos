-- liquibase formatted sql

-- changeset cnpe:1746241822380-1
CREATE SEQUENCE IF NOT EXISTS primary_sequence START WITH 10000 INCREMENT BY 1;

-- changeset cnpe:1746241822380-2
CREATE TABLE order_items
(
    id                       BIGINT           NOT NULL,
    version                  INTEGER          NOT NULL,
    created_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by               VARCHAR(255)     NOT NULL,
    last_modified_by         VARCHAR(255),
    order_id                 BIGINT           NOT NULL,
    product_id               BIGINT           NOT NULL,
    product_name_snapshot    VARCHAR(255)     NOT NULL,
    sku_snapshot             VARCHAR(255),
    quantity                 DOUBLE PRECISION NOT NULL,
    applied_discount_rule_id BIGINT,
    unit_price_amount        DECIMAL          NOT NULL,
    unit_price_currency      VARCHAR(3)       NOT NULL,
    net_unit_price_amount    DECIMAL          NOT NULL,
    net_unit_price_currency  VARCHAR(3)       NOT NULL,
    discount_amount          DECIMAL          NOT NULL,
    discount_currency        VARCHAR(3)       NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

-- changeset cnpe:1746241822380-3
CREATE TABLE orders
(
    id                             BIGINT       NOT NULL,
    version                        INTEGER      NOT NULL,
    created_at                     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by                     VARCHAR(255) NOT NULL,
    last_modified_by               VARCHAR(255),
    order_number                   VARCHAR(255) NOT NULL,
    status                         VARCHAR(255) NOT NULL,
    branch_id                      BIGINT       NOT NULL,
    user_idp_id                    VARCHAR(255) NOT NULL,
    customer_id                    BIGINT,
    order_timestamp                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    applied_order_discount_rule_id BIGINT,
    notes                          VARCHAR(500),
    sub_total_amount               DECIMAL      NOT NULL,
    sub_total_currency             VARCHAR(3)   NOT NULL,
    tax_amount                     DECIMAL      NOT NULL,
    tax_currency                   VARCHAR(3)   NOT NULL,
    total_amount                   DECIMAL      NOT NULL,
    total_currency                 VARCHAR(3)   NOT NULL,
    discount_amount                DECIMAL      NOT NULL,
    discount_currency              VARCHAR(3)   NOT NULL,
    order_level_discount_amount    DECIMAL      NOT NULL,
    order_level_discount_currency  VARCHAR(3)   NOT NULL,
    final_amount                   DECIMAL      NOT NULL,
    final_currency                 VARCHAR(3)   NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

-- changeset cnpe:1746241822380-4
ALTER TABLE orders
    ADD CONSTRAINT uc_orders_order_number UNIQUE (order_number);

-- changeset cnpe:1746241822380-5
ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

