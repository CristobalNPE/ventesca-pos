-- liquibase formatted sql

-- changeset cnpe:sessions-001-create-sessions-number-sequence
CREATE SEQUENCE session_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;