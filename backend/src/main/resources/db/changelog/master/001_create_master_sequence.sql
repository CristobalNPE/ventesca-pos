-- changeset Cristóbal:master-primary-sequence-001-create
CREATE SEQUENCE primary_sequence
    START WITH 10000
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;