package com.sotatek.warehouse.validation;

import com.sotatek.warehouse.dto.request.ReservationItemRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class UniqueSkusValidator implements ConstraintValidator<UniqueSkus, List<ReservationItemRequest>> {

    @Override
    public boolean isValid(List<ReservationItemRequest> items, ConstraintValidatorContext context) {
        if (items == null) {
            return true;
        }
        long distinctSkus = items.stream().map(ReservationItemRequest::sku).distinct().count();
        return distinctSkus == items.size();
    }
}
