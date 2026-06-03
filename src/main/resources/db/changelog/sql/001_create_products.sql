-- liquibase formatted sql

-- changeset warehouse:001_create_products
CREATE TABLE products (
    id          BIGSERIAL    PRIMARY KEY,
    sku         VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

-- rollback DROP TABLE products;
