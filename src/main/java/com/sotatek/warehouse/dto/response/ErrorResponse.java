package com.sotatek.warehouse.dto.response;

public record ErrorResponse(
        String code,
        String message
) {
}
