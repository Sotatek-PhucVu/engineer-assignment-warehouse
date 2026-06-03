-- liquibase formatted sql

-- changeset warehouse:006_reservations_order_id_unique
-- Enforce idempotency: a given orderId can map to at most one reservation,
-- so retried/duplicate submissions cannot create duplicate reservations or double-deduct stock.
ALTER TABLE reservations ADD CONSTRAINT uq_reservations_order_id UNIQUE (order_id);

-- rollback ALTER TABLE reservations DROP CONSTRAINT uq_reservations_order_id;
