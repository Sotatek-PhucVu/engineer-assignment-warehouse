package com.sotatek.warehouse.dto.response;

public record ApiResponse<T>(
        T data,
        ErrorResponse error
) {
}
