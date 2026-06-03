-- liquibase formatted sql

-- changeset warehouse:004_create_reservation_items
CREATE TABLE reservation_items (
    id             BIGSERIAL   PRIMARY KEY,
    reservation_id BIGINT      NOT NULL REFERENCES reservations(id),
    sku            VARCHAR(50) NOT NULL,
    quantity       INTEGER     NOT NULL CHECK (quantity > 0)
);

-- rollback DROP TABLE reservation_items;
