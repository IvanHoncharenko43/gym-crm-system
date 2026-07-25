package org.example.core.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "The 'from' date cannot be after the 'to' date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
