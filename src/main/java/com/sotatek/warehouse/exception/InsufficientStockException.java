package com.sotatek.warehouse.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String sku, int available, int requested) {
        super("SKU %s has only %d available, %d requested".formatted(sku, available, requested));
    }
}