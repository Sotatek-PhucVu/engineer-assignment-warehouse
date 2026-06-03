package com.sotatek.warehouse.service.state;


import com.sotatek.warehouse.entity.Reservation;

public interface ReservationState {

    void confirm(Reservation reservation);

    void cancel(Reservation reservation);
}
