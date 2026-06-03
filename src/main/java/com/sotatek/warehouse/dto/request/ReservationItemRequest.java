package com.sotatek.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationItemRequest(

        @NotBlank
        String sku,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
