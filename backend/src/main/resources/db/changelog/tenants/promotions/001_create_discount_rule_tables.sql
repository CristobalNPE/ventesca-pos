-- liquibase formatted sql

-- changeset cnpe:1746229108265-2
CREATE TABLE discount_rule_target_brands
(
    discount_rule_id BIGINT NOT NULL,
    brand_id         BIGINT
);

-- changeset cnpe:1746229108265-3
CREATE TABLE discount_rule_target_categories
(
    discount_rule_id BIGINT NOT NULL,
    category_id      BIGINT
);

-- changeset cnpe:1746229108265-4
CREATE TABLE discount_rule_target_products
(
    discount_rule_id BIGINT NOT NULL,
    product_id       BIGINT
);

-- changeset cnpe:1746229108265-5
CREATE TABLE discount_rules
(
    id                     BIGINT                      NOT NULL,
    version                INTEGER                     NOT NULL,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by             VARCHAR(255)                NOT NULL,
    last_modified_by       VARCHAR(255),
    name                   VARCHAR(255)                NOT NULL,
    description            VARCHAR(255),
    type                   VARCHAR(255)                NOT NULL,
    value                  DECIMAL(10, 2)              NOT NULL,
    start_date             TIMESTAMP WITHOUT TIME ZONE,
    end_date               TIMESTAMP WITHOUT TIME ZONE,
    is_active              BOOLEAN                     NOT NULL,
    applicability          VARCHAR(255)                NOT NULL,
    minimum_quantity       INTEGER,
    is_combinable          BOOLEAN                     NOT NULL,
    minimum_spend_amount   DECIMAL,
    minimum_spend_currency VARCHAR(3),
    CONSTRAINT pk_discount_rules PRIMARY KEY (id)
);

-- changeset cnpe:1746229108265-6
ALTER TABLE discount_rules
    ADD CONSTRAINT uc_discount_rules_name UNIQUE (name);

-- changeset cnpe:1746229108265-7
CREATE INDEX idx_discount_rules_active_dates
    ON discount_rules (is_active, start_date, end_date);

-- changeset cnpe:1746229108265-8
CREATE INDEX idx_discount_rules_applicability
    ON discount_rules (applicability);

-- changeset cnpe:1746229108265-9
ALTER TABLE discount_rule_target_brands
    ADD CONSTRAINT fk_discount_rule_target_brands_on_discount_rule FOREIGN KEY (discount_rule_id) REFERENCES discount_rules (id);

-- changeset cnpe:1746229108265-10
ALTER TABLE discount_rule_target_categories
    ADD CONSTRAINT fk_discount_rule_target_categories_on_discount_rule FOREIGN KEY (discount_rule_id) REFERENCES discount_rules (id);

-- changeset cnpe:1746229108265-11
ALTER TABLE discount_rule_target_products
    ADD CONSTRAINT fk_discount_rule_target_products_on_discount_rule FOREIGN KEY (discount_rule_id) REFERENCES discount_rules (id);

-- changeset cnpe:index-for-dr-prod
CREATE INDEX idx_dr_target_prod_product
    ON discount_rule_target_products (product_id);

-- changeset cnpe:index-for-dr-cat
CREATE INDEX idx_dr_target_cat_category
    ON discount_rule_target_categories (category_id);

-- changeset cnpe:index-for-dr-brand
CREATE INDEX idx_dr_target_brand_brand
    ON discount_rule_target_brands (brand_id);