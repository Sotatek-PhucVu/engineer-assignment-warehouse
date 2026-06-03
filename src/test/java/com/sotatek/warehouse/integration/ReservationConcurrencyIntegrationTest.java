package com.sotatek.warehouse.integration;

import com.sotatek.warehouse.dto.request.CreateReservationRequest;
import com.sotatek.warehouse.dto.request.ReservationItemRequest;
import com.sotatek.warehouse.dto.response.ReservationResponse;
import com.sotatek.warehouse.exception.InsufficientStockException;
import com.sotatek.warehouse.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetInventory() {
        jdbcTemplate.update(
                "DELETE FROM reservation_items WHERE reservation_id IN " +
                "(SELECT id FROM reservations WHERE order_id LIKE 'ORD-CONCURRENT-%')"
        );
        jdbcTemplate.update("DELETE FROM reservations WHERE order_id LIKE 'ORD-CONCURRENT-%'");
        jdbcTemplate.update(
                "UPDATE inventory SET available_stock = 100, reserved_stock = 0, version = 0 WHERE sku = 'A100'"
        );
    }

    @Test
    void concurrentReservations_exactlyOneSucceeds_whenCombinedQuantityExceedsStock()
            throws InterruptedException {

        // A100 has 100 units; two threads each try to reserve 60 → only one can succeed
        int numThreads = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        List<Exception> failures = new CopyOnWriteArrayList<>();
        List<ReservationResponse> successes = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final String orderId = "ORD-CONCURRENT-" + i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    ReservationResponse response = reservationService.reserve(
                            new CreateReservationRequest(orderId, List.of(new ReservationItemRequest("A100", 60)))
                    );
                    successes.add(response);
                } catch (Exception e) {
                    failures.add(e);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertThat(doneLatch.await(15, TimeUnit.SECONDS)).isTrue();

        assertThat(successes).hasSize(1);
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOf(InsufficientStockException.class);

        // Verify inventory state: 60 reserved, 40 available
        Integer available = jdbcTemplate.queryForObject(
                "SELECT available_stock FROM inventory WHERE sku = 'A100'", Integer.class
        );
        assertThat(available).isEqualTo(40);
    }
}
