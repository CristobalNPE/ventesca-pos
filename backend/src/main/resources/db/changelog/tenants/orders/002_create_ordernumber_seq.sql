-- liquibase formatted sql

-- changeset cnpe:orders-001-create-order-number-sequence
CREATE SEQUENCE order_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;