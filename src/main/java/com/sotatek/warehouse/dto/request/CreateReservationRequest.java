package com.sotatek.warehouse.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateReservationRequest(

        @NotBlank
        String orderId,

        @NotEmpty
        @Valid
        List<ReservationItemRequest> items
) {
}