package com.sotatek.warehouse.exception;

import com.sotatek.warehouse.dto.response.ApiResponse;
import com.sotatek.warehouse.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(null, new ErrorResponse("NOT_FOUND", ex.getMessage())));
    }

    @ExceptionHandler(InvalidReservationStateException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidState(InvalidReservationStateException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(null, new ErrorResponse("INVALID_RESERVATION_STATE", ex.getMessage())));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(null, new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(null, new ErrorResponse("VALIDATION_ERROR", message)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(null, new ErrorResponse("MALFORMED_REQUEST", "Request body is missing or malformed")));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiResponse<>(null, new ErrorResponse("METHOD_NOT_ALLOWED", ex.getMessage())));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<?>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(null, new ErrorResponse("CONCURRENT_MODIFICATION", "Resource was modified concurrently, please retry")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred")));
    }
}
