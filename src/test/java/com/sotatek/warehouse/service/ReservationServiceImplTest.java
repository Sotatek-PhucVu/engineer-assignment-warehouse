package com.sotatek.warehouse.service;

import com.sotatek.warehouse.dto.request.CreateReservationRequest;
import com.sotatek.warehouse.dto.request.ReservationItemRequest;
import com.sotatek.warehouse.dto.response.ReservationResponse;
import com.sotatek.warehouse.entity.Inventory;
import com.sotatek.warehouse.entity.Reservation;
import com.sotatek.warehouse.entity.ReservationItem;
import com.sotatek.warehouse.entity.ReservationStatus;
import com.sotatek.warehouse.exception.DuplicateException;
import com.sotatek.warehouse.exception.InsufficientStockException;
import com.sotatek.warehouse.exception.InvalidReservationStateException;
import com.sotatek.warehouse.exception.ResourceNotFoundException;
import com.sotatek.warehouse.repository.InventoryRepository;
import com.sotatek.warehouse.repository.ReservationRepository;
import com.sotatek.warehouse.service.factory.ReservationFactory;
import com.sotatek.warehouse.service.ReservationServiceImpl;
import com.sotatek.warehouse.service.state.ReservationStateFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationFactory reservationFactory;

    private ReservationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReservationServiceImpl(
                inventoryRepository,
                reservationRepository,
                reservationFactory,
                new ReservationStateFactory()
        );
    }

    // ---- reserve() ----
    @Test
    void reserve_success_reduces_available_stock() {
        Inventory inventory = buildInventory("A100", 100, 0);
        Reservation reservation = buildReservation(1L, ReservationStatus.PENDING);

        when(reservationRepository.findByOrderId("ORD-001")).thenReturn(Optional.empty());
        when(inventoryRepository.findBySkuInForUpdate(List.of("A100"))).thenReturn(List.of(inventory));
        when(reservationFactory.create(any())).thenReturn(reservation);
        when(reservationRepository.save(any())).thenReturn(reservation);

        ReservationResponse response = service.reserve(
                new CreateReservationRequest("ORD-001", List.of(new ReservationItemRequest("A100", 30)))
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(inventory.getAvailableStock()).isEqualTo(70);
        assertThat(inventory.getReservedStock()).isEqualTo(30);
    }

    @Test
    void reserve_throwsDuplicateException_whenOrderIdAlreadyExists() {
        Reservation existing = buildReservation(7L, ReservationStatus.PENDING);
        when(reservationRepository.findByOrderId("ORD-DUP")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.reserve(
                new CreateReservationRequest("ORD-DUP", List.of(new ReservationItemRequest("A100", 30)))
        ))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("ORD-DUP");

        // Duplicate is rejected before any stock lookup or deduction
        verify(inventoryRepository, never()).findBySkuInForUpdate(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserve_throws_InsufficientStockException_when_stock_too_low() {
        Inventory inventory = buildInventory("A100", 10, 0);
        when(reservationRepository.findByOrderId("ORD-001")).thenReturn(Optional.empty());
        when(inventoryRepository.findBySkuInForUpdate(List.of("A100"))).thenReturn(List.of(inventory));

        assertThatThrownBy(() -> service.reserve(
                new CreateReservationRequest("ORD-001", List.of(new ReservationItemRequest("A100", 30)))
        ))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("A100")
                .hasMessageContaining("10")
                .hasMessageContaining("30");
    }

    @Test
    void reserve_throws_ResourceNotFoundException_when_sku_not_found() {
        when(reservationRepository.findByOrderId("ORD-001")).thenReturn(Optional.empty());
        when(inventoryRepository.findBySkuInForUpdate(List.of("UNKNOWN"))).thenReturn(List.of());

        assertThatThrownBy(() -> service.reserve(
                new CreateReservationRequest("ORD-001", List.of(new ReservationItemRequest("UNKNOWN", 5)))
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    // ---- confirm() ----
    @Test
    void confirm_transitions_PENDING_to_CONFIRMED() {
        Reservation reservation = buildReservation(1L, ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);

        ReservationResponse response = service.confirm(1L);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void confirm_throws_InvalidStateException_when_already_CONFIRMED() {
        Reservation reservation = buildReservation(1L, ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.confirm(1L))
                .isInstanceOf(InvalidReservationStateException.class)
                .hasMessageContaining("already CONFIRMED");
    }

    @Test
    void confirm_throws_InvalidStateException_when_CANCELLED() {
        Reservation reservation = buildReservation(1L, ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.confirm(1L))
                .isInstanceOf(InvalidReservationStateException.class)
                .hasMessageContaining("CANCELLED");
    }

    // ---- cancel() ----
    @Test
    void cancel_transitions_PENDING_to_CANCELLED_and_restores_stock() {
        ReservationItem item = buildReservationItem("A100", 30);
        Reservation reservation = buildReservation(1L, ReservationStatus.PENDING, List.of(item));
        Inventory inventory = buildInventory("A100", 70, 30);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findBySkuIn(List.of("A100"))).thenReturn(List.of(inventory));
        when(reservationRepository.save(any())).thenReturn(reservation);

        ReservationResponse response = service.cancel(1L);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(inventory.getAvailableStock()).isEqualTo(100);
        assertThat(inventory.getReservedStock()).isEqualTo(0);
    }

    @Test
    void cancel_throws_InvalidStateException_when_CONFIRMED() {
        Reservation reservation = buildReservation(1L, ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(InvalidReservationStateException.class)
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    void cancel_throws_InvalidStateException_when_already_CANCELLED() {
        Reservation reservation = buildReservation(1L, ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(InvalidReservationStateException.class)
                .hasMessageContaining("already CANCELLED");
    }

    // ---- getReservation() ----
    @Test
    void getReservation_returns_response_when_found() {
        Reservation reservation = buildReservation(1L, ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = service.getReservation(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.orderId()).isEqualTo("ORD-1");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void getReservation_throws_ResourceNotFoundException_when_not_found() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReservation(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- helpers ----
    private Inventory buildInventory(String sku, int available, int reserved) {
        Inventory inv = new Inventory();
        inv.setSku(sku);
        inv.setTotalStock(available + reserved);
        inv.setAvailableStock(available);
        inv.setReservedStock(reserved);
        return inv;
    }

    private Reservation buildReservation(Long id, ReservationStatus status) {
        return buildReservation(id, status, List.of());
    }

    private Reservation buildReservation(Long id, ReservationStatus status, List<ReservationItem> items) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setOrderId("ORD-" + id);
        r.setStatus(status);
        r.setCreatedAt(LocalDateTime.now());
        items.forEach(item -> {
            item.setReservation(r);
            r.getItems().add(item);
        });
        return r;
    }

    private ReservationItem buildReservationItem(String sku, int quantity) {
        ReservationItem item = new ReservationItem();
        item.setSku(sku);
        item.setQuantity(quantity);
        return item;
    }
}
