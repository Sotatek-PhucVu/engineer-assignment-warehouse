package com.sotatek.warehouse.service;

import com.sotatek.warehouse.dto.request.CreateReservationRequest;
import com.sotatek.warehouse.dto.request.ReservationItemRequest;
import com.sotatek.warehouse.dto.response.ReservationItemResponse;
import com.sotatek.warehouse.dto.response.ReservationResponse;
import com.sotatek.warehouse.entity.Inventory;
import com.sotatek.warehouse.entity.Reservation;
import com.sotatek.warehouse.entity.ReservationItem;
import com.sotatek.warehouse.exception.InsufficientStockException;
import com.sotatek.warehouse.exception.ResourceNotFoundException;
import com.sotatek.warehouse.repository.InventoryRepository;
import com.sotatek.warehouse.repository.ReservationRepository;
import com.sotatek.warehouse.service.factory.ReservationFactory;
import com.sotatek.warehouse.service.state.ReservationStateFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationFactory reservationFactory;
    private final ReservationStateFactory stateFactory;

    @Transactional
    @Override
    public ReservationResponse reserve(CreateReservationRequest request) {
        List<String> skus = request.items().stream()
                .map(ReservationItemRequest::sku)
                .toList();

        // Fetch and lock all rows in one query.
        // ORDER BY sku ensures consistent lock ordering across concurrent transactions, preventing deadlocks.
        Map<String, Inventory> inventoryMap = inventoryRepository
                .findBySkuInForUpdate(skus)
                .stream()
                .collect(Collectors.toMap(Inventory::getSku, i -> i));

        // Validate stock and deduct atomically within the same transaction.
        for (var item : request.items()) {
            Inventory inventory = Optional.ofNullable(inventoryMap.get(item.sku()))
                    .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + item.sku()));

            if (inventory.getAvailableStock() < item.quantity()) {
                throw new InsufficientStockException(
                        item.sku(), inventory.getAvailableStock(), item.quantity()
                );
            }

            inventory.setAvailableStock(inventory.getAvailableStock() - item.quantity());
            inventory.setReservedStock(inventory.getReservedStock() + item.quantity());
        }

        Reservation reservation = reservationFactory.create(request);
        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional
    @Override
    public ReservationResponse confirm(Long id) {
        Reservation reservation = findById(id);
        stateFactory.getState(reservation.getStatus()).confirm(reservation);
        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional
    @Override
    public ReservationResponse cancel(Long id) {
        Reservation reservation = findById(id);
        stateFactory.getState(reservation.getStatus()).cancel(reservation);

        List<String> skus = reservation.getItems().stream()
                .map(ReservationItem::getSku)
                .toList();

        Map<String, Inventory> inventoryMap = inventoryRepository
                .findBySkuIn(skus)
                .stream()
                .collect(Collectors.toMap(Inventory::getSku, i -> i));

        for (ReservationItem item : reservation.getItems()) {
            Inventory inventory = Optional.ofNullable(inventoryMap.get(item.getSku()))
                    .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + item.getSku()));
            inventory.setAvailableStock(inventory.getAvailableStock() + item.getQuantity());
            inventory.setReservedStock(inventory.getReservedStock() - item.getQuantity());
        }

        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationResponse getReservation(Long id) {
        return toResponse(findById(id));
    }

    private Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        List<ReservationItemResponse> items = reservation.getItems().stream()
                .map(item -> new ReservationItemResponse(item.getSku(), item.getQuantity()))
                .toList();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                items
        );
    }
}
