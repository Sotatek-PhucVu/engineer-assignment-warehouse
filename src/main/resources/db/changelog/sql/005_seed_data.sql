-- liquibase formatted sql

-- changeset warehouse:005_seed_data
INSERT INTO products (sku, name, description) VALUES
    ('A100', 'Product A100', 'Sample product A100'),
    ('B200', 'Product B200', 'Sample product B200'),
    ('C300', 'Product C300', 'Sample product C300');

INSERT INTO inventory (sku, total_stock, available_stock, reserved_stock, version) VALUES
    ('A100', 100, 100, 0, 0),
    ('B200', 50,  50,  0, 0),
    ('C300', 200, 200, 0, 0);

-- rollback DELETE FROM inventory; DELETE FROM products;
