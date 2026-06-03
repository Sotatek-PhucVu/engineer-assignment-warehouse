-- liquibase formatted sql

-- changeset warehouse:002_create_inventory
CREATE TABLE inventory (
    id              BIGSERIAL    PRIMARY KEY,
    sku             VARCHAR(50)  NOT NULL UNIQUE REFERENCES products(sku),
    total_stock     INTEGER      NOT NULL DEFAULT 0 CHECK (total_stock >= 0),
    available_stock INTEGER      NOT NULL DEFAULT 0 CHECK (available_stock >= 0),
    reserved_stock  INTEGER      NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),
    version         BIGINT       NOT NULL DEFAULT 0
);

-- rollback DROP TABLE inventory;
