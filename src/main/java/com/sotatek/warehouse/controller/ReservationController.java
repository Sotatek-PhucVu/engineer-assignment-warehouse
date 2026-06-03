package com.sotatek.warehouse.controller;

import com.sotatek.warehouse.dto.request.CreateReservationRequest;
import com.sotatek.warehouse.dto.response.ApiResponse;
import com.sotatek.warehouse.dto.response.ReservationResponse;
import com.sotatek.warehouse.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> reserve(@Valid @RequestBody CreateReservationRequest request) {
        return new ApiResponse<>(reservationService.reserve(request), null);
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<ReservationResponse> confirm(@PathVariable Long id) {
        return new ApiResponse<>(reservationService.confirm(id), null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancel(@PathVariable Long id) {
        return new ApiResponse<>(reservationService.cancel(id), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable Long id) {
        return new ApiResponse<>(reservationService.getReservation(id), null);
    }
}
