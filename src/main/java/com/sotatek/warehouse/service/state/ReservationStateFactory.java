package com.sotatek.warehouse.service.state;

import com.sotatek.warehouse.entity.ReservationStatus;
import org.springframework.stereotype.Component;

@Component
public class ReservationStateFactory {

    public ReservationState getState(ReservationStatus status) {
        return switch (status) {
            case PENDING    -> new PendingState();
            case CONFIRMED  -> new ConfirmedState();
            case CANCELLED  -> new CancelledState();
        };
    }
}
