package com.sotatek.warehouse.service.factory;


import com.sotatek.warehouse.dto.request.CreateReservationRequest;
import com.sotatek.warehouse.dto.request.ReservationItemRequest;
import com.sotatek.warehouse.entity.Reservation;
import com.sotatek.warehouse.entity.ReservationItem;
import com.sotatek.warehouse.entity.ReservationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationFactory {

    public Reservation create(CreateReservationRequest request) {

        Reservation reservation = new Reservation();

        reservation.setOrderId(request.orderId());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());

        for (ReservationItemRequest itemRequest : request.items()) {

            ReservationItem item = new ReservationItem();

            item.setSku(itemRequest.sku());
            item.setQuantity(itemRequest.quantity());
            item.setReservation(reservation);

            reservation.getItems().add(item);
        }

        return reservation;
    }
}
