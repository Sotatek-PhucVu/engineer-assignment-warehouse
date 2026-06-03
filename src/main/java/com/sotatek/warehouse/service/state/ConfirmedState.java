package com.sotatek.warehouse.service.state;

import com.sotatek.warehouse.entity.Reservation;
import com.sotatek.warehouse.exception.InvalidReservationStateException;

public class ConfirmedState implements ReservationState {
    @Override
    public void confirm(Reservation reservation) {
        throw new InvalidReservationStateException("Reservation is already CONFIRMED");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new InvalidReservationStateException("CONFIRMED reservation cannot be cancelled");
    }
}
