-- liquibase formatted sql

-- changeset Cristóbal:1745631242203-2
CREATE TABLE product_images
(
    product_id  BIGINT       NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    image_order INTEGER      NOT NULL,
    CONSTRAINT pk_product_images PRIMARY KEY (product_id, image_order)
);

-- changeset Cristóbal:1745631242203-3
CREATE TABLE product_prices
(
    id                     BIGINT                      NOT NULL,
    version                INTEGER                     NOT NULL,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by             VARCHAR(255)                NOT NULL,
    last_modified_by       VARCHAR(255),
    product_id             BIGINT                      NOT NULL,
    start_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date               TIMESTAMP WITHOUT TIME ZONE,
    reason                 VARCHAR(255)                NOT NULL,
    selling_price_amount   DECIMAL                     NOT NULL,
    selling_price_currency VARCHAR(3)                  NOT NULL,
    cost_price_amount      DECIMAL                     NOT NULL,
    cost_price_currency    VARCHAR(3)                  NOT NULL,
    CONSTRAINT pk_product_prices PRIMARY KEY (id)
);

-- changeset Cristóbal:1745631242203-4
CREATE TABLE products
(
    id                  BIGINT                      NOT NULL,
    version             INTEGER                     NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)                NOT NULL,
    last_modified_by    VARCHAR(255),
    name                VARCHAR(255)                NOT NULL,
    sku                 VARCHAR(255),
    barcode             VARCHAR(255),
    status              VARCHAR(255)                NOT NULL,
    description         TEXT,
    total_current_stock DOUBLE PRECISION,
    category_id         BIGINT                      NOT NULL,
    brand_id            BIGINT                      NOT NULL,
    supplier_id         BIGINT                      NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

-- changeset Cristóbal:1745631242203-5
ALTER TABLE products
    ADD CONSTRAINT uk_product_barcode UNIQUE (barcode);

-- changeset Cristóbal:1745631242203-6
ALTER TABLE products
    ADD CONSTRAINT uk_product_sku UNIQUE (sku);

-- changeset Cristóbal:1745631242203-7
CREATE INDEX idx_product_barcode ON products (barcode);

-- changeset Cristóbal:1745631242203-8
CREATE INDEX idx_product_sku ON products (sku);

-- changeset Cristóbal:1745631242203-9
ALTER TABLE product_prices
    ADD CONSTRAINT FK_PRODUCT_PRICES_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

-- changeset Cristóbal:1745631242203-10
ALTER TABLE product_images
    ADD CONSTRAINT fk_product_images_on_product FOREIGN KEY (product_id) REFERENCES products (id);

