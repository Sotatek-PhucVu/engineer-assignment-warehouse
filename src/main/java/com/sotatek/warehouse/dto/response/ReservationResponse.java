package com.sotatek.warehouse.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
        Long id,
        String orderId,
        String status,
        LocalDateTime createdAt,
        List<ReservationItemResponse> items
) {
}
