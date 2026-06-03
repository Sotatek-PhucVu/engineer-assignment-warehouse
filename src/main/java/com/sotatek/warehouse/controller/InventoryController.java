package com.sotatek.warehouse.controller;

import com.sotatek.warehouse.dto.response.ApiResponse;
import com.sotatek.warehouse.dto.response.InventoryResponse;
import com.sotatek.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    public ApiResponse<InventoryResponse> getStock(@PathVariable String sku) {
        return new ApiResponse<>(inventoryService.getStock(sku), null);
    }
}
