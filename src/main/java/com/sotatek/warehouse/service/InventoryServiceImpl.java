package com.sotatek.warehouse.service;

import com.sotatek.warehouse.dto.response.InventoryResponse;
import com.sotatek.warehouse.entity.Inventory;
import com.sotatek.warehouse.exception.ResourceNotFoundException;
import com.sotatek.warehouse.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    @Override
    public InventoryResponse getStock(String sku) {
        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + sku));
        return new InventoryResponse(
                inventory.getSku(),
                inventory.getTotalStock(),
                inventory.getAvailableStock(),
                inventory.getReservedStock()
        );
    }
}
