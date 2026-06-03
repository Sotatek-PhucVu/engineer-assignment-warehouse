-- liquibase formatted sql

-- changeset warehouse:003_create_reservations
CREATE TABLE reservations (
    id         BIGSERIAL    PRIMARY KEY,
    order_id   VARCHAR(100) NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0
);

-- rollback DROP TABLE reservations;
