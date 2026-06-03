package com.sotatek.warehouse.service.state;

import com.sotatek.warehouse.entity.Reservation;
import com.sotatek.warehouse.exception.InvalidReservationStateException;

public class CancelledState implements ReservationState {
    @Override
    public void confirm(Reservation reservation) {
        throw new InvalidReservationStateException("CANCELLED reservation cannot be confirmed");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new InvalidReservationStateException("Reservation is already CANCELLED");
    }
}
