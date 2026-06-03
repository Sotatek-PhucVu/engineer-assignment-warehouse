package com.sotatek.warehouse.exception;

import org.springframework.http.HttpStatus;

/**
 * Central registry of API error codes and the HTTP status each maps to.
 * The {@code code} returned to clients is the enum name; messages stay contextual
 * and are supplied at the throw site.
 */
public enum ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_RESERVATION_STATE(HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST),
    DUPLICATE(HttpStatus.CONFLICT),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
