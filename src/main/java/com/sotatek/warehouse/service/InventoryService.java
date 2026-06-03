package com.sotatek.warehouse.service;

import com.sotatek.warehouse.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse getStock(String sku);
}
