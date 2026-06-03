package com.sotatek.warehouse.dto.request;

import com.sotatek.warehouse.validation.UniqueSkus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateReservationRequest(

        @NotBlank
        String orderId,

        @NotEmpty
        @Valid
        @UniqueSkus
        List<ReservationItemRequest> items
) {
}