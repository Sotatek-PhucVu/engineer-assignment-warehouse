package com.sotatek.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReservationItemRequest(

        @NotBlank
        String sku,

        @Min(1)
        Integer quantity
) {
}