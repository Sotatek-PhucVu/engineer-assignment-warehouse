package com.sotatek.warehouse.service;

import com.sotatek.warehouse.dto.request.CreateReservationRequest;
import com.sotatek.warehouse.dto.response.ReservationResponse;

public interface ReservationService {

    ReservationResponse reserve(CreateReservationRequest request);

    ReservationResponse confirm(Long id);

    ReservationResponse cancel(Long id);

    ReservationResponse getReservation(Long id);
}
