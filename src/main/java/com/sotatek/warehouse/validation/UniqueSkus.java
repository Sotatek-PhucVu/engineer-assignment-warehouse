package com.sotatek.warehouse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that a list of reservation items contains no duplicate SKUs.
 */
@Documented
@Constraint(validatedBy = UniqueSkusValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface UniqueSkus {

    String message() default "must not contain duplicate SKUs";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
