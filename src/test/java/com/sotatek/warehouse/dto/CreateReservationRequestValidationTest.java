package com.sotatek.warehouse.dto;

import com.sotatek.warehouse.dto.request.CreateReservationRequest;
import com.sotatek.warehouse.dto.request.ReservationItemRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateReservationRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void valid_request_hasNoViolations() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1", List.of(new ReservationItemRequest("A100", 5)));
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void nullQuantity_isRejected() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1", List.of(new ReservationItemRequest("A100", null)));
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().contains("quantity"));
    }

    @Test
    void zeroQuantity_isRejected() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1", List.of(new ReservationItemRequest("A100", 0)));
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().contains("quantity"));
    }

    @Test
    void blankSku_isRejected() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1", List.of(new ReservationItemRequest("  ", 5)));
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().contains("sku"));
    }

    @Test
    void emptyItems_isRejected() {
        CreateReservationRequest request = new CreateReservationRequest("ORD-1", List.of());
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().contains("items"));
    }

    @Test
    void blankOrderId_isRejected() {
        CreateReservationRequest request = new CreateReservationRequest(
                "", List.of(new ReservationItemRequest("A100", 5)));
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().contains("orderId"));
    }

    @Test
    void duplicateSkus_isRejected() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1", List.of(
                        new ReservationItemRequest("A100", 5),
                        new ReservationItemRequest("A100", 3)));
        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().contains("items")
                        && v.getMessage().contains("duplicate SKUs"));
    }

    @Test
    void distinctSkus_areAccepted() {
        CreateReservationRequest request = new CreateReservationRequest(
                "ORD-1", List.of(
                        new ReservationItemRequest("A100", 5),
                        new ReservationItemRequest("B200", 3)));
        assertThat(validator.validate(request)).isEmpty();
    }
}
