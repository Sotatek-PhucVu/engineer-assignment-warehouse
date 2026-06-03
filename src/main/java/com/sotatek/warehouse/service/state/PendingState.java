package com.sotatek.warehouse.service.state;

import com.sotatek.warehouse.entity.Reservation;
import com.sotatek.warehouse.entity.ReservationStatus;

public class PendingState implements ReservationState {
    @Override
    public void confirm(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CONFIRMED);
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
    }
}
