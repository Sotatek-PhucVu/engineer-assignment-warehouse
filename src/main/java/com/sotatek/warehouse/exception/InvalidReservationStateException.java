package com.sotatek.warehouse.exception;

public class InvalidReservationStateException extends RuntimeException {

    public InvalidReservationStateException(String message) {
        super(message);
    }
}