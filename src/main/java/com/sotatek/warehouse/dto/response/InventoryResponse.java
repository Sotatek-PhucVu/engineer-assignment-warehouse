package com.sotatek.warehouse.dto.response;

public record InventoryResponse(
        String sku,
        Integer totalStock,
        Integer availableStock,
        Integer reservedStock
) {
}
