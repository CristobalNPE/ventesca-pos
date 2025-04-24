-- liquibase formatted sql

-- changeset Cristóbal:business-branches-001-create-table
CREATE TABLE business_branches
(
    id                BIGINT                      NOT NULL,
    version           INTEGER                     NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by        VARCHAR(255)                NOT NULL,
    last_modified_by  VARCHAR(255),
    business_id       BIGINT                      NOT NULL,
    branch_name       VARCHAR(255)                NOT NULL,
    contact_number    VARCHAR(255),
    is_main_branch    BOOLEAN                     NOT NULL,
    branch_manager_id VARCHAR(255),
    street            VARCHAR(100),
    city              VARCHAR(50),
    country           VARCHAR(50),
    postal_code       VARCHAR(20),
    CONSTRAINT pk_business_branches PRIMARY KEY (id)
);

-- changeset Cristóbal:business-payment-methods-001-create-table
CREATE TABLE business_payment_methods
(
    business_id    BIGINT       NOT NULL,
    payment_method VARCHAR(255) NOT NULL
);

-- changeset Cristóbal:business-users-001-create-table
CREATE TABLE business_users
(
    id               BIGINT                      NOT NULL,
    version          INTEGER                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by       VARCHAR(255)                NOT NULL,
    last_modified_by VARCHAR(255),
    user_email       VARCHAR(255)                NOT NULL,
    business_id      BIGINT,
    CONSTRAINT pk_business_users PRIMARY KEY (id)
);

-- changeset Cristóbal:businesses-001-create-table
CREATE TABLE businesses
(
    id                BIGINT                      NOT NULL,
    version           INTEGER                     NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by        VARCHAR(255)                NOT NULL,
    last_modified_by  VARCHAR(255),
    admin_id          VARCHAR(255)                NOT NULL,
    tenant_id         VARCHAR(255)                NOT NULL,
    business_name     VARCHAR(50)                 NOT NULL,
    logo_url          VARCHAR(255),
    brand_message     VARCHAR(500),
    phone             VARCHAR(25),
    email             VARCHAR(255),
    website           VARCHAR(255),
    currency_code     VARCHAR(255),
    tax_percentage    DECIMAL,
    status            VARCHAR(255)                NOT NULL,
    status_reason     VARCHAR(255),
    status_changed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_businesses PRIMARY KEY (id)
);

-- changeset Cristóbal:businesses-001-unique-tenantId
ALTER TABLE businesses
    ADD CONSTRAINT uc_businesses_tenant UNIQUE (tenant_id);

-- changeset Cristóbal:businesses-001-idx-userEmail
CREATE INDEX idx_business_user_email ON business_users (user_email);

-- changeset Cristóbal:business-branches-001-fk-businessId
ALTER TABLE business_branches
    ADD CONSTRAINT FK_BUSINESS_BRANCHES_ON_BUSINESS FOREIGN KEY (business_id) REFERENCES businesses (id);

-- changeset Cristóbal:business-users-001-fk-businessId
ALTER TABLE business_users
    ADD CONSTRAINT FK_BUSINESS_USERS_ON_BUSINESS FOREIGN KEY (business_id) REFERENCES businesses (id);

-- changeset Cristóbal:business-payment-methods-001-fk-businessId
ALTER TABLE business_payment_methods
    ADD CONSTRAINT fk_business_payment_methods_on_business FOREIGN KEY (business_id) REFERENCES businesses (id);

